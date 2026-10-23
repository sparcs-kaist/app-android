package org.sparcs.soap.app.features.timetable.activity

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityParsingError
import org.sparcs.soap.app.domain.models.otl.ActivityParsingException
import org.sparcs.soap.app.domain.models.otl.ParsedActivity
import org.sparcs.soap.app.domain.usecases.otl.ActivityParsingUseCaseProtocol
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

data class ActivityQuickFillState(
    val isAvailable: Boolean = false,
    val isParsing: Boolean = false,
    @param:StringRes val errorRes: Int? = null,
)

@HiltViewModel
class ActivityQuickFillViewModel @Inject constructor(
    private val parsingUseCase: ActivityParsingUseCaseProtocol,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ActivityQuickFillState())
    val state: StateFlow<ActivityQuickFillState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val available = try {
                parsingUseCase.isAvailable()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Timber.w(error, "Activity Quick Fill availability check failed")
                false
            }
            mutableState.update { it.copy(isAvailable = available) }
        }
    }

    fun parse(description: String, onParsed: (ParsedActivity) -> Unit) {
        if (description.isBlank() || mutableState.value.isParsing) return
        mutableState.update { it.copy(isParsing = true, errorRes = null) }
        viewModelScope.launch {
            try {
                onParsed(parsingUseCase.parse(description, today()))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: ActivityParsingException) {
                mutableState.update { it.copy(errorRes = error.reason.messageRes) }
            } catch (error: Exception) {
                Timber.w(error, "Activity Quick Fill failed")
                mutableState.update { it.copy(errorRes = ActivityParsingError.FAILED.messageRes) }
            } finally {
                mutableState.update { it.copy(isParsing = false) }
            }
        }
    }

    fun clearError() {
        if (mutableState.value.errorRes != null) mutableState.update { it.copy(errorRes = null) }
    }

    private fun today(): DayType =
        DayType.fromValue(LocalDate.now().dayOfWeek.value - 1) ?: DayType.MON

    private val ActivityParsingError.messageRes: Int
        get() = when (this) {
            ActivityParsingError.UNAVAILABLE -> R.string.activity_quick_fill_unavailable
            ActivityParsingError.UNSAFE_REQUEST -> R.string.activity_quick_fill_unsafe
            ActivityParsingError.NOTHING_FOUND -> R.string.activity_quick_fill_nothing_found
            ActivityParsingError.FAILED -> R.string.activity_quick_fill_failed
        }
}
