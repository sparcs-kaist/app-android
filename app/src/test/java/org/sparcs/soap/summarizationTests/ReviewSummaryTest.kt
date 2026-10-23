package org.sparcs.soap.summarizationTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryError
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryException
import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryUseCase
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewSummaryUseCase
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewSummaryTest {
    private val longReview = "강의가 체계적이고 과제가 매주 있어요. ".repeat(12)
    private val reviews = MockReviewSummaryUseCase()
    private val delegate = TextProcessingDelegate(
        object : PostTranslationUseCaseProtocol {
            override fun availableLanguages() = emptyList<String>()
            override fun suggestedLanguages() = emptyList<String>()
            override fun deviceLanguage() = "en"
            override suspend fun translate(text: String, targetLanguage: String, isHtml: Boolean, allowDownload: Boolean) =
                PostTranslationResult.Unsupported
        },
        object : SummarizationUseCaseProtocol {
            override suspend fun isAvailable() = false
            override suspend fun summarise(text: String, isHtml: Boolean) = SummarizationResultState.Unavailable
        },
        reviews,
    )

    private fun TestScope.summarize(review: String = longReview) =
        delegate.summarizeReview(review, TestScope(UnconfinedTestDispatcher(testScheduler)))

    @Test
    fun `streamed review summary ends as the full summary`() = runTest {
        reviews.chunks = listOf("Clear lectures", "Clear lectures, weekly homework.")
        summarize()
        assertEquals(SummarizationState.Summarized("Clear lectures, weekly homework."), delegate.summarizationState.value)
    }

    @Test
    fun `short review is not sent to the model`() = runTest {
        summarize("Too short to need a summary.")
        assertEquals(SummarizationState.TooShort, delegate.summarizationState.value)
        assertEquals(0, reviews.streamCallCount)
    }

    @Test
    fun `failure is shown as a failure, never as the summary`() = runTest {
        reviews.chunks = listOf("Partial")
        reviews.failure = ReviewSummaryException(ReviewSummaryError.FAILED)
        summarize()
        assertEquals(SummarizationState.Failed, delegate.summarizationState.value)
    }

    @Test
    fun `blocked review shows the unsafe message`() = runTest {
        reviews.failure = ReviewSummaryException(ReviewSummaryError.UNSAFE_CONTENT)
        summarize()
        assertEquals(SummarizationState.Unsafe, delegate.summarizationState.value)
    }

    @Test
    fun `empty response is a failure`() = runTest {
        reviews.chunks = listOf("   ")
        summarize()
        assertEquals(SummarizationState.Failed, delegate.summarizationState.value)
    }

    @Test
    fun `failed summary can be retried`() = runTest {
        reviews.failure = IllegalStateException("offline")
        summarize()
        assertEquals(SummarizationState.Failed, delegate.summarizationState.value)
        reviews.failure = null
        reviews.chunks = listOf("Fair grading.")
        summarize()
        assertEquals(SummarizationState.Summarized("Fair grading."), delegate.summarizationState.value)
        assertEquals(2, reviews.streamCallCount)
    }

    @Test
    fun `review length threshold counts trimmed characters`() {
        assertTrue(ReviewSummaryUseCase.canSummarise("a".repeat(ReviewSummaryUseCase.MIN_REVIEW_LENGTH)))
        assertFalse(ReviewSummaryUseCase.canSummarise(" ".repeat(50) + "a".repeat(ReviewSummaryUseCase.MIN_REVIEW_LENGTH - 1)))
    }

    @Test
    fun `prompt names the device language and keeps the review`() {
        val language = ReviewSummaryUseCase.deviceLanguage(Locale.KOREAN)
        assertEquals("Korean", language)
        val prompt = ReviewSummaryUseCase.prompt("Great TA support.", language)
        assertTrue(prompt.contains("Write in Korean"))
        assertTrue(prompt.endsWith("Great TA support."))
    }
}
