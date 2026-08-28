package org.sparcs.soap.app.domain.usecases.summarization

import android.content.Context
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.sparcs.soap.app.shared.extensions.htmlToPlainText
import java.util.Locale
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface SummarizationResultState {
    data class Success(val summary: String) : SummarizationResultState
    data object TooShort : SummarizationResultState
    data object Unavailable : SummarizationResultState
    data class Failed(val error: Throwable) : SummarizationResultState
}

interface SummarizationUseCaseProtocol {
    suspend fun isAvailable(): Boolean
    suspend fun summarise(text: String, isHtml: Boolean): SummarizationResultState
}

class SummarizationUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SummarizationUseCaseProtocol {
    private fun languageOrNull(): Int? = when (Locale.getDefault().language) {
        "ko" -> SummarizerOptions.Language.KOREAN
        "ja" -> SummarizerOptions.Language.JAPANESE
        "en" -> SummarizerOptions.Language.ENGLISH
        else -> null
    }

    private fun newSummarizer(language: Int): Summarizer {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)
            .setLanguage(language)
            .setLongInputAutoTruncationEnabled(true)
            .build()
        return Summarization.getClient(options)
    }

    override suspend fun isAvailable(): Boolean {
        val language = languageOrNull() ?: return false
        val summarizer = newSummarizer(language)
        return try {
            summarizer.checkFeatureStatus().await() != FeatureStatus.UNAVAILABLE
        } catch (_: Exception) {
            false
        } finally {
            summarizer.close()
        }
    }

    override suspend fun summarise(text: String, isHtml: Boolean): SummarizationResultState {
        val language = languageOrNull() ?: return SummarizationResultState.Unavailable
        val plain = if (isHtml) text.htmlToPlainText() else text.trim()
        if (plain.length < MIN_LENGTH) return SummarizationResultState.TooShort

        val summarizer = newSummarizer(language)
        return try {
            when (summarizer.checkFeatureStatus().await()) {
                FeatureStatus.UNAVAILABLE -> SummarizationResultState.Unavailable

                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                    summarizer.downloadFeature(noopDownloadCallback).await()
                    runInference(summarizer, plain)
                }

                else -> runInference(summarizer, plain)
            }
        } catch (e: Exception) {
            SummarizationResultState.Failed(e)
        } finally {
            summarizer.close()
        }
    }

    private suspend fun runInference(summarizer: Summarizer, text: String): SummarizationResultState {
        val request = SummarizationRequest.builder(text).build()
        val result = summarizer.runInference(request).await()
        return SummarizationResultState.Success(result.summary)
    }

    private val noopDownloadCallback = object : DownloadCallback {
        override fun onDownloadStarted(bytesToDownload: Long) {}
        override fun onDownloadProgress(totalBytesDownloaded: Long) {}
        override fun onDownloadCompleted() {}
        override fun onDownloadFailed(e: GenAiException) {}
    }

    private suspend fun <T> ListenableFuture<T>.await(): T =
        suspendCancellableCoroutine { cont ->
            addListener(
                {
                    try {
                        cont.resume(Futures.getDone(this))
                    } catch (e: ExecutionException) {
                        cont.resumeWithException(e.cause ?: e)
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                },
                { it.run() }
            )
            cont.invokeOnCancellation { cancel(false) }
        }

    private companion object {
        const val MIN_LENGTH = 400
    }
}
