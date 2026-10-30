package org.sparcs.soap.app.domain.usecases.summarization

import android.content.Context
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.sparcs.soap.app.domain.usecases.reviewDigest.LectureReviewBatching
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

    suspend fun isPostSummaryAvailable(): Boolean = isAvailable()

    fun streamPostSummary(text: String): Flow<String> = flow {
        when (val result = summarise(text, isHtml = false)) {
            is SummarizationResultState.Success -> emit(result.summary)
            SummarizationResultState.Unavailable -> throw ReviewSummaryException(ReviewSummaryError.UNAVAILABLE)
            SummarizationResultState.TooShort, is SummarizationResultState.Failed ->
                throw ReviewSummaryException(ReviewSummaryError.FAILED)
        }
    }
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
        if (plain.length < MIN_POST_LENGTH) return SummarizationResultState.TooShort

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

    override suspend fun isPostSummaryAvailable(): Boolean {
        val model = Generation.getClient()
        return try {
            model.checkStatus() != FeatureStatus.UNAVAILABLE
        } catch (_: Exception) {
            false
        } finally {
            model.close()
        }
    }

    override fun streamPostSummary(text: String): Flow<String> = flow {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw ReviewSummaryException(ReviewSummaryError.FAILED)
        val model = Generation.getClient()
        try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> Unit
                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> model.download().collect {}
                else -> throw ReviewSummaryException(ReviewSummaryError.UNAVAILABLE)
            }
            val language = ReviewSummaryUseCase.deviceLanguage()
            val source = fitting(model, trimmed, language)
            val request = generateContentRequest(TextPart(summaryPrompt(source, language))) {
                temperature = 0f
                topK = 1
                maxOutputTokens = SUMMARY_TOKENS
            }
            val response = StringBuilder()
            model.generateContentStream(request).collect { chunk ->
                response.append(chunk.candidates.firstOrNull()?.text.orEmpty())
                emit(PostSummaryFormat.text(response.toString()))
            }
            if (PostSummaryFormat.text(response.toString()).isBlank()) throw ReviewSummaryException(ReviewSummaryError.FAILED)
        } catch (error: GenAiException) {
            throw ReviewSummaryException(error.summaryError)
        } finally {
            model.close()
        }
    }

    private suspend fun fitting(model: GenerativeModel, text: String, language: String): String {
        val limit = runCatching { model.getTokenLimit() }.getOrDefault(DEFAULT_TOKEN_LIMIT)
        val budget = maxOf(limit - RESERVED_TOKENS, MINIMUM_BUDGET)
        if (LectureReviewBatching.estimatedTokens(text) <= budget) return text
        val parts = LectureReviewBatching.batches(paragraphs(text), budget) { LectureReviewBatching.estimatedTokens(it) }
        return parts.map { part ->
            val request = generateContentRequest(TextPart(notesPrompt(part.joinToString(LINE_SEPARATOR), language))) {
                temperature = 0f
                topK = 1
                maxOutputTokens = NOTES_TOKENS
            }
            model.generateContent(request).candidates.firstOrNull()?.text.orEmpty()
        }.filter { it.isNotBlank() }.joinToString(LINE_SEPARATOR)
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

    companion object {
        const val MIN_POST_LENGTH = 400
        const val MAX_PARAGRAPH_LENGTH = 1_500
        const val SUMMARY_WORD_LIMIT = 60
        private const val DEFAULT_TOKEN_LIMIT = 4_000
        private const val RESERVED_TOKENS = 800
        private const val MINIMUM_BUDGET = 1_000
        private const val SUMMARY_TOKENS = 256
        private const val NOTES_TOKENS = 200
        private const val LINE_SEPARATOR = "\n"

        fun paragraphs(text: String): List<String> = text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { it.chunked(MAX_PARAGRAPH_LENGTH) }

        fun summaryPrompt(post: String, language: String): String = """
            You summarise a post on a university community board, for someone deciding whether to read it in full. Keep the key facts: subjects, names, dates, times, places and numbers. Leave out greetings unless they are the point. Copy every number, amount and unit exactly as the post writes it: Korean amounts such as 3만원 mean 30,000 won, so keep 3만원 rather than converting it.

            Keep every phrase short, and never repeat a fact. Write in $language, whatever language the post is in. Write plain text only, with no Markdown. Answer in exactly this format, keeping the English labels:
            GIST: one short sentence saying what the post is, such as a job offer, an event or a question. Never start with "This post" or "The post".
            FACTS:
            - the facts a reader would act on, each a short phrase: when, where, how much, who can take part, and what to do or whom to contact. Most important first, at most ${PostSummaryFormat.MAXIMUM_FACTS}, or None

            Summarise this in $language, in at most $SUMMARY_WORD_LIMIT words:
            $post
        """.trimIndent()

        fun notesPrompt(part: String, language: String): String = """
            You take notes on one part of a longer text, for someone who will summarise the whole text later. Write at most five short bullet points in $language with the key facts in this part: subjects, names, dates, times, places, numbers, and anything the reader is asked to do. Leave out greetings.

            Take notes on this part:
            $part
        """.trimIndent()
    }
}
