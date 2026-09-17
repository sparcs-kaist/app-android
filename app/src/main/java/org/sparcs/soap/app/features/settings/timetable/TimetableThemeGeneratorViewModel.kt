package org.sparcs.soap.app.features.settings.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.domain.helpers.TimetableThemeBrief
import org.sparcs.soap.app.domain.usecases.ThemeGenerationError
import org.sparcs.soap.app.domain.usecases.ThemeGenerationException
import org.sparcs.soap.app.domain.usecases.ThemeGenerationUseCase
import org.sparcs.soap.app.domain.usecases.ThemeModelStatus
import org.sparcs.soap.app.domain.usecases.TimetableThemeGenerationUseCase
import timber.log.Timber
import kotlin.random.Random

internal data class ThemeGeneratorState(
    val description: String = "",
    val modelStatus: ThemeModelStatus = ThemeModelStatus.CHECKING,
    val generating: Boolean = false,
    val downloading: Boolean = false,
    val ready: Boolean = false,
    val preview: TimetableTheme? = null,
    val error: ThemeGenerationError? = null,
) {
    val canGenerate get() = description.isNotBlank() && modelStatus == ThemeModelStatus.AVAILABLE && !generating && !downloading
}

internal class TimetableThemeGeneratorViewModel(
    private val useCase: ThemeGenerationUseCase = TimetableThemeGenerationUseCase(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(ThemeGeneratorState())
    val state = mutableState.asStateFlow()
    private var generationJob: Job? = null
    private var availabilityJob: Job? = null

    fun refreshAvailability() {
        if (state.value.downloading || state.value.generating) return
        availabilityJob?.cancel()
        availabilityJob = viewModelScope.launch {
            val status = try {
                useCase.availability()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                ThemeModelStatus.UNAVAILABLE
            }
            currentCoroutineContext().ensureActive()
            mutableState.update { it.copy(modelStatus = status) }
        }
    }

    fun describe(value: String) {
        if (state.value.generating) return
        mutableState.update {
            it.copy(
                description = value.take(TimetableThemeBrief.maximumDescriptionLength),
                ready = false,
                preview = null,
                error = null
            )
        }
    }

    fun download() {
        if (state.value.downloading || state.value.generating ||
            state.value.modelStatus !in setOf(
                ThemeModelStatus.DOWNLOADABLE,
                ThemeModelStatus.DOWNLOADING
            )
        ) return
        availabilityJob?.cancel()
        mutableState.update { it.copy(downloading = true, error = null) }
        generationJob = viewModelScope.launch {
            try {
                useCase.download()
                currentCoroutineContext().ensureActive()
                mutableState.update {
                    it.copy(
                        modelStatus = ThemeModelStatus.AVAILABLE,
                        downloading = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                mutableState.update {
                    it.copy(
                        downloading = false,
                        error = ThemeGenerationError.DOWNLOAD_FAILED
                    )
                }
            }
        }
    }

    fun generate(base: TimetableTheme, prompt: String) {
        if (!state.value.canGenerate) return
        generationJob?.cancel()
        availabilityJob?.cancel()
        mutableState.update {
            it.copy(
                generating = true,
                ready = false,
                preview = null,
                error = null
            )
        }
        generationJob = viewModelScope.launch {
            try {
                var latest: TimetableTheme? = null
                flow {
                    emitAll(useCase.generate(prompt, Random.nextInt(1, Int.MAX_VALUE)))
                }.onStart {
                    latest = null
                    mutableState.update { it.copy(preview = null) }
                }.retryWhen { error, attempt ->
                    currentCoroutineContext().ensureActive()
                    val reason = (error as? ThemeGenerationException)?.reason
                    val retry =
                        attempt == 0L && (reason == ThemeGenerationError.BUSY || reason == ThemeGenerationError.INCOMPLETE)
                    if (retry) {
                        Timber.w("Retrying timetable theme generation: reason=%s", reason)
                        delay(400)
                    }
                    retry
                }.collect { brief ->
                    currentCoroutineContext().ensureActive()
                    latest = brief.theme(base)
                    latest?.let { theme -> mutableState.update { it.copy(preview = theme) } }
                }
                currentCoroutineContext().ensureActive()
                if (latest == null) throw ThemeGenerationException(ThemeGenerationError.INCOMPLETE)
                mutableState.update { it.copy(generating = false, ready = true, preview = latest) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                currentCoroutineContext().ensureActive()
                val reason =
                    (error as? ThemeGenerationException)?.reason ?: ThemeGenerationError.FAILED
                Timber.w("Timetable theme generation failed: reason=%s", reason)
                mutableState.update {
                    it.copy(
                        generating = false, ready = false, preview = null, error = reason,
                        modelStatus = if (reason == ThemeGenerationError.UNAVAILABLE) ThemeModelStatus.UNAVAILABLE else it.modelStatus,
                    )
                }
            }
        }
    }

    fun cancel() {
        generationJob?.cancel()
        mutableState.update {
            it.copy(
                generating = false,
                downloading = false,
                preview = if (it.ready) it.preview else null
            )
        }
    }

    fun reset() {
        cancel()
        availabilityJob?.cancel()
        mutableState.update { ThemeGeneratorState(modelStatus = it.modelStatus) }
    }
}
