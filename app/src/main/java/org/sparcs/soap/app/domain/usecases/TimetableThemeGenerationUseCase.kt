package org.sparcs.soap.app.domain.usecases

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import org.sparcs.soap.app.domain.helpers.TimetableThemeBrief
import timber.log.Timber

enum class ThemeModelStatus { CHECKING, AVAILABLE, DOWNLOADABLE, DOWNLOADING, UNAVAILABLE }
enum class ThemeGenerationError { UNAVAILABLE, UNSAFE_REQUEST, INCOMPLETE, BUSY, QUOTA_EXCEEDED, FAILED, DOWNLOAD_FAILED }
class ThemeGenerationException(val reason: ThemeGenerationError) : Exception()

interface ThemeGenerationUseCase {
    suspend fun availability(): ThemeModelStatus
    suspend fun download()
    fun generate(prompt: String, variation: Int): Flow<TimetableThemeBrief>
}

class TimetableThemeGenerationUseCase : ThemeGenerationUseCase {
    override suspend fun availability(): ThemeModelStatus {
        val model = Generation.getClient()
        return try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> ThemeModelStatus.AVAILABLE
                FeatureStatus.DOWNLOADABLE -> ThemeModelStatus.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> ThemeModelStatus.DOWNLOADING
                else -> ThemeModelStatus.UNAVAILABLE
            }
        } finally {
            model.close()
        }
    }

    override suspend fun download() {
        val model = Generation.getClient()
        try {
            model.download().collect { status ->
                if (status is DownloadStatus.DownloadFailed) throw ThemeGenerationException(
                    ThemeGenerationError.DOWNLOAD_FAILED
                )
            }
            if (model.checkStatus() != FeatureStatus.AVAILABLE) throw ThemeGenerationException(
                ThemeGenerationError.DOWNLOAD_FAILED
            )
        } finally {
            model.close()
        }
    }

    override fun generate(prompt: String, variation: Int): Flow<TimetableThemeBrief> =
        flow<TimetableThemeBrief> {
            val model = Generation.getClient()
            try {
                if (model.checkStatus() != FeatureStatus.AVAILABLE) throw ThemeGenerationException(
                    ThemeGenerationError.UNAVAILABLE
                )
                model.warmup()
                val request = generateContentRequest(TextPart(prompt)) {
                    temperature = 0.65f
                    seed = variation
                    maxOutputTokens = 512
                }
                val response = StringBuilder()
                model.generateContentStream(request).collect { chunk ->
                    response.append(chunk.candidates.firstOrNull()?.text.orEmpty())
                    emit(ThemeBriefParser.partial(response.toString()))
                }
                emit(ThemeBriefParser.complete(response.toString()))
            } catch (error: GenAiException) {
                Timber.w("Timetable theme AICore failure: code=%d", error.errorCode)
                throw ThemeGenerationException(
                    when (error.errorCode) {
                        GenAiException.ErrorCode.BUSY -> ThemeGenerationError.BUSY
                        GenAiException.ErrorCode.PER_APP_BATTERY_USE_QUOTA_EXCEEDED -> ThemeGenerationError.QUOTA_EXCEEDED
                        GenAiException.ErrorCode.REQUEST_PROCESSING_ERROR,
                        GenAiException.ErrorCode.RESPONSE_GENERATION_ERROR,
                        GenAiException.ErrorCode.RESPONSE_PROCESSING_ERROR,
                            -> ThemeGenerationError.UNSAFE_REQUEST

                        GenAiException.ErrorCode.NOT_AVAILABLE,
                        GenAiException.ErrorCode.NOT_SUPPORTED,
                        GenAiException.ErrorCode.AICORE_INCOMPATIBLE,
                        GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE,
                            -> ThemeGenerationError.UNAVAILABLE

                        else -> ThemeGenerationError.FAILED
                    }
                )
            } finally {
                model.close()
            }
        }.distinctUntilChanged()
}
