package org.sparcs.soap.app.domain.usecases.summarization

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Locale
import javax.inject.Inject

enum class ReviewSummaryError { UNAVAILABLE, UNSAFE_CONTENT, FAILED }

class ReviewSummaryException(val reason: ReviewSummaryError) : Exception(reason.name)

interface ReviewSummaryUseCaseProtocol {
    suspend fun isAvailable(): Boolean
    fun streamSummary(review: String): Flow<String>
}

class ReviewSummaryUseCase @Inject constructor() : ReviewSummaryUseCaseProtocol {
    override suspend fun isAvailable(): Boolean {
        val model = Generation.getClient()
        return try {
            model.checkStatus() != FeatureStatus.UNAVAILABLE
        } catch (_: Exception) {
            false
        } finally {
            model.close()
        }
    }

    override fun streamSummary(review: String): Flow<String> = flow {
        val text = review.trim()
        if (text.isEmpty()) throw ReviewSummaryException(ReviewSummaryError.FAILED)
        val model = Generation.getClient()
        try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> Unit
                FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> model.download().collect {}
                else -> throw ReviewSummaryException(ReviewSummaryError.UNAVAILABLE)
            }
            val request = generateContentRequest(TextPart(prompt(text, deviceLanguage()))) {
                temperature = 0f
                maxOutputTokens = MAX_OUTPUT_TOKENS
            }
            val summary = StringBuilder()
            model.generateContentStream(request).collect { chunk ->
                summary.append(chunk.candidates.firstOrNull()?.text.orEmpty())
                emit(summary.toString())
            }
            if (summary.isBlank()) throw ReviewSummaryException(ReviewSummaryError.FAILED)
        } catch (error: GenAiException) {
            throw ReviewSummaryException(error.reason)
        } finally {
            model.close()
        }
    }

    private val GenAiException.reason: ReviewSummaryError
        get() = when (errorCode) {
            GenAiException.ErrorCode.REQUEST_PROCESSING_ERROR,
            GenAiException.ErrorCode.RESPONSE_GENERATION_ERROR,
            GenAiException.ErrorCode.RESPONSE_PROCESSING_ERROR,
                -> ReviewSummaryError.UNSAFE_CONTENT

            GenAiException.ErrorCode.NOT_AVAILABLE,
            GenAiException.ErrorCode.NOT_SUPPORTED,
            GenAiException.ErrorCode.AICORE_INCOMPATIBLE,
            GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE,
                -> ReviewSummaryError.UNAVAILABLE

            else -> ReviewSummaryError.FAILED
        }

    companion object {
        const val MIN_REVIEW_LENGTH = 200
        private const val MAX_WORDS = 40
        private const val MAX_OUTPUT_TOKENS = 160

        fun canSummarise(review: String): Boolean = review.trim().length >= MIN_REVIEW_LENGTH

        fun deviceLanguage(locale: Locale = Locale.getDefault()): String =
            locale.getDisplayLanguage(Locale.ENGLISH).ifBlank { "English" }

        fun prompt(review: String, language: String): String = """
            You summarise one student's review of a university lecture, for someone deciding whether to take it. Keep what the review says about the lecturing, the workload, the exams and assignments, the grading, and any advice it gives. Leave out remarks that say nothing about the lecture.

            Write plain sentences, without a heading or bullet points. Write in $language, whatever language the review is in.

            Summarise this review in $language, in at most $MAX_WORDS words:
            $review
        """.trimIndent()
    }
}
