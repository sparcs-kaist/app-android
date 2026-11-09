package org.sparcs.soap.app.domain.usecases.reviewDigest

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestError
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestException
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestProgress
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

interface ReviewDigestUseCaseProtocol {
    suspend fun isAvailable(): Boolean
    fun streamDigest(reviews: List<LectureReview>): Flow<LectureReviewDigestProgress>
}

class ReviewDigestUseCase @Inject constructor() : ReviewDigestUseCaseProtocol {
    override suspend fun isAvailable(): Boolean {
        if (BuildConfig.DEBUG) return true
        val model = Generation.getClient()
        return try {
            model.checkStatus() == FeatureStatus.AVAILABLE
        } catch (error: Exception) {
            Timber.w(error, "Review digest availability check failed")
            false
        } finally {
            model.close()
        }
    }

    override fun streamDigest(reviews: List<LectureReview>): Flow<LectureReviewDigestProgress> = flow {
        val lines = LectureReviewBatching.lines(reviews)
        if (lines.isEmpty()) throw LectureReviewDigestException(LectureReviewDigestError.NO_REVIEWS)
        val model = Generation.getClient()
        try {
            if (BuildConfig.DEBUG && !model.isReady()) {
                emitFakeDigest()
                return@flow
            }
            if (model.checkStatus() != FeatureStatus.AVAILABLE) {
                throw LectureReviewDigestException(LectureReviewDigestError.UNAVAILABLE)
            }
            model.warmup()
            val language = deviceLanguage()
            val budget = maxOf(model.tokenLimit() - RESERVED_TOKENS, MINIMUM_BUDGET)
            val source = readSource(model, lines, budget, language)
            val response = StringBuilder()
            val request = generateContentRequest(TextPart(digestPrompt(source, language))) {
                temperature = 0f
                topK = 1
                maxOutputTokens = DIGEST_TOKENS
            }
            retryingWhenBusy {
                response.clear()
                model.generateContentStream(request).collect { chunk ->
                    response.append(chunk.candidates.firstOrNull()?.text.orEmpty())
                    emit(LectureReviewDigestProgress.Writing(ReviewDigestParser.parse(response.toString())))
                }
            }
        } catch (error: GenAiException) {
            Timber.w("Review digest AICore failure: code=%d", error.errorCode)
            throw LectureReviewDigestException(error.reason())
        } finally {
            model.close()
        }
    }

    private suspend fun FlowCollector<LectureReviewDigestProgress>.readSource(
        model: GenerativeModel,
        lines: List<String>,
        budget: Int,
        language: String,
    ): String {
        val batches = LectureReviewBatching.batches(lines, budget) { LectureReviewBatching.estimatedTokens(it) }
        emit(LectureReviewDigestProgress.Started(batches.sumOf { it.size }))
        if (batches.size == 1) return batches.single().joinToString("\n")
        val notes = batches.mapIndexed { index, batch ->
            emit(LectureReviewDigestProgress.Reading(index, batches.size))
            try {
                retryingWhenBusy { model.respond(notesPrompt(batch.joinToString("\n"), language), NOTES_TOKENS) }
            } catch (error: GenAiException) {
                if (error.errorCode != GenAiException.ErrorCode.REQUEST_TOO_LARGE || budget <= MINIMUM_BUDGET) throw error
                return readSource(model, lines, budget / 2, language)
            }
        }
        emit(LectureReviewDigestProgress.Reading(batches.size, batches.size))
        return notes.filter { it.isNotBlank() }.joinToString("\n")
            .ifBlank { throw LectureReviewDigestException(LectureReviewDigestError.FAILED) }
    }

    private suspend fun GenerativeModel.isReady(): Boolean =
        runCatching { checkStatus() == FeatureStatus.AVAILABLE }.getOrDefault(false)

    /** Debug-only stand-in for devices without Gemini Nano, so the digest card can still be seen. */
    private suspend fun FlowCollector<LectureReviewDigestProgress>.emitFakeDigest() {
        emit(LectureReviewDigestProgress.Started(FAKE_REVIEW_COUNT))
        repeat(FAKE_BATCHES + 1) { completed ->
            emit(LectureReviewDigestProgress.Reading(completed, FAKE_BATCHES))
            delay(FAKE_STEP_MILLIS)
        }
        val steps = listOf(
            LectureReviewDigest(overview = FAKE_DIGEST.overview),
            LectureReviewDigest(overview = FAKE_DIGEST.overview, liked = FAKE_DIGEST.liked),
            FAKE_DIGEST.copy(workload = null, grading = null),
            FAKE_DIGEST.copy(grading = null),
            FAKE_DIGEST,
        )
        steps.forEach {
            emit(LectureReviewDigestProgress.Writing(it))
            delay(FAKE_STEP_MILLIS)
        }
    }

    private suspend fun <T> retryingWhenBusy(block: suspend () -> T): T {
        repeat(BUSY_RETRIES) { attempt ->
            try {
                return block()
            } catch (error: GenAiException) {
                if (error.errorCode != GenAiException.ErrorCode.BUSY) throw error
                delay(BUSY_BACKOFF_MILLIS shl attempt)
            }
        }
        return block()
    }

    private suspend fun GenerativeModel.tokenLimit(): Int =
        runCatching { getTokenLimit() }.getOrDefault(DEFAULT_TOKEN_LIMIT)

    private suspend fun GenerativeModel.respond(prompt: String, maxTokens: Int): String {
        val request = generateContentRequest(TextPart(prompt)) {
            temperature = 0f
            topK = 1
            maxOutputTokens = maxTokens
        }
        return generateContent(request).candidates.firstOrNull()?.text.orEmpty()
    }

    private fun GenAiException.reason(): LectureReviewDigestError = when (errorCode) {
        GenAiException.ErrorCode.BUSY,
        GenAiException.ErrorCode.PER_APP_BATTERY_USE_QUOTA_EXCEEDED,
            -> LectureReviewDigestError.BUSY

        GenAiException.ErrorCode.BACKGROUND_USE_BLOCKED -> LectureReviewDigestError.INTERRUPTED

        GenAiException.ErrorCode.REQUEST_PROCESSING_ERROR,
        GenAiException.ErrorCode.RESPONSE_GENERATION_ERROR,
        GenAiException.ErrorCode.RESPONSE_PROCESSING_ERROR,
            -> LectureReviewDigestError.UNSAFE_CONTENT

        GenAiException.ErrorCode.NOT_AVAILABLE,
        GenAiException.ErrorCode.NOT_SUPPORTED,
        GenAiException.ErrorCode.AICORE_INCOMPATIBLE,
        GenAiException.ErrorCode.NEEDS_SYSTEM_UPDATE,
            -> LectureReviewDigestError.UNAVAILABLE

        else -> LectureReviewDigestError.FAILED
    }

    private fun deviceLanguage(): String = when (Locale.getDefault().language) {
        "ko" -> "Korean"
        "ja" -> "Japanese"
        else -> "English"
    }

    private fun digestPrompt(reviews: String, language: String) = """
        You summarise what university students say about a lecture in their course reviews, or in notes taken from those reviews.

        $RATINGS_EXPLANATION

        Report only what the reviews say, giving what most reviewers agree on rather than what a single reviewer says. Never name a reviewer, and never invent details such as exam counts or percentages that no review gives.

        Write in $language, whatever language the reviews are in. Keep every phrase short. Write plain text only, with no Markdown: no headings, bold, italics or asterisks. Answer in exactly this format, keeping the English labels:
        OVERVIEW: one or two sentences on the overall impression.
        LIKED:
        - what students liked, a few words each, most mentioned first, at most 4
        DISLIKED:
        - what students disliked or warned about, a few words each, at most 4, or None
        WORKLOAD: one short sentence on assignments, exams and the time it takes.
        GRADING: one short sentence on how generously the lecture is graded.

        Summarise these reviews in $language:
        $reviews
    """.trimIndent()

    private fun notesPrompt(reviews: String, language: String) = """
        You take notes on university students' course reviews of a lecture, for someone who will summarise them later.

        $RATINGS_EXPLANATION

        Write at most six short lines in $language, each starting with "- ", covering the overall impression, what students liked, what they disliked, the workload and the grading. Say when several reviews agree. Leave out anything personal or offensive. Write plain text only, with no Markdown: no headings, bold, italics or asterisks.

        Take notes on these reviews:
        $reviews
    """.trimIndent()

    private companion object {
        const val RATINGS_EXPLANATION = "Each review is on its own line and starts with its ratings in brackets. Grade is how generously the lecture is graded, Load is how light its workload is, and Speech is how good the lecturing is. Higher ratings are better in all three."
        const val DEFAULT_TOKEN_LIMIT = 4000
        const val RESERVED_TOKENS = 1200
        const val MINIMUM_BUDGET = 1000
        const val NOTES_TOKENS = 256
        const val DIGEST_TOKENS = 512
        const val BUSY_RETRIES = 3
        const val BUSY_BACKOFF_MILLIS = 1_500L
        const val FAKE_REVIEW_COUNT = 100
        const val FAKE_BATCHES = 3
        const val FAKE_STEP_MILLIS = 400L
        val FAKE_DIGEST = LectureReviewDigest(
            overview = "전반적으로 강의는 이해하기 쉬우며 유익하다고 평가됨. 일부는 성적이 후하게 주어지지만, 과제와 채점 기준에 불만을 표함.",
            liked = listOf("이해하기 쉬움", "유익함", "설명 상세함", "초보자 적합"),
            disliked = listOf("과제 부담", "채점 기준 모호함", "플젝 난이도", "코드 설명 불명확함"),
            workload = "과제 부담이 크고, 특히 플젝과 코드 설명이 어려움.",
            grading = "성적이 일반적으로 후하게 주어짐.",
        )
    }
}
