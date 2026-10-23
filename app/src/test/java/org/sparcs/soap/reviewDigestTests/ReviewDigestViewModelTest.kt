package org.sparcs.soap.reviewDigestTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestError
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestException
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestProgress
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestPhase
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewDigestUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewDigestViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: MockReviewDigestUseCase
    private lateinit var crashlytics: MockCrashlyticsService
    private lateinit var viewModel: ReviewDigestViewModel

    private val reviews = (1..3).map { LectureReview.mock().copy(id = it, content = "review $it") }
    private val digest = LectureReviewDigest(overview = "Good", liked = listOf("Clear"))

    @Before
    fun setup() {
        useCase = MockReviewDigestUseCase()
        crashlytics = MockCrashlyticsService()
        viewModel = ReviewDigestViewModel(useCase, MockAnalyticsService(), crashlytics)
        useCase.updates = listOf(
            LectureReviewDigestProgress.Started(3),
            LectureReviewDigestProgress.Reading(1, 2),
            LectureReviewDigestProgress.Writing(digest),
        )
    }

    @Test
    fun `nothing runs before availability is known`() = runTest {
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(0, useCase.streamCallCount)
        assertEquals(ReviewDigestPhase.Idle, viewModel.state.value.phase)
    }

    @Test
    fun `automatic summary finishes with the streamed digest`() = runTest {
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(ReviewDigestPhase.Finished(digest), viewModel.state.value.phase)
        assertEquals(3, viewModel.state.value.reviewCount)
    }

    @Test
    fun `fewer than three readable reviews are not summarised`() = runTest {
        viewModel.checkAvailability()
        val few = reviews.take(2) + reviews[2].copy(content = "  ")
        assertFalse(viewModel.canSummarise(few))
        viewModel.summariseIfNeeded(few, automatically = true, screen = "Test")
        assertEquals(0, useCase.streamCallCount)
    }

    @Test
    fun `unavailable model never summarises`() = runTest {
        useCase.available = false
        viewModel.checkAvailability()
        assertFalse(viewModel.canSummarise(reviews))
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(0, useCase.streamCallCount)
    }

    @Test
    fun `manual mode waits for a request`() = runTest {
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = false, screen = "Test")
        assertEquals(0, useCase.streamCallCount)
        viewModel.requestSummary(reviews, "Test")
        assertEquals(1, useCase.streamCallCount)
        assertEquals(ReviewDigestPhase.Finished(digest), viewModel.state.value.phase)
    }

    @Test
    fun `finished summary is not run again for the same reviews`() = runTest {
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(1, useCase.streamCallCount)
    }

    @Test
    fun `changed reviews are summarised again`() = runTest {
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        viewModel.summariseIfNeeded(reviews + LectureReview.mock().copy(id = 9, content = "more"), automatically = true, screen = "Test")
        assertEquals(2, useCase.streamCallCount)
    }

    @Test
    fun `a digest without an overview fails`() = runTest {
        useCase.updates = listOf(LectureReviewDigestProgress.Writing(LectureReviewDigest(liked = listOf("x"))))
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(ReviewDigestPhase.Failed(LectureReviewDigestError.FAILED), viewModel.state.value.phase)
        assertEquals(1, crashlytics.recordExceptionCallCount)
    }

    @Test
    fun `digest errors keep their reason and are not reported`() = runTest {
        useCase.updates = emptyList()
        useCase.failure = LectureReviewDigestException(LectureReviewDigestError.UNSAFE_CONTENT)
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(ReviewDigestPhase.Failed(LectureReviewDigestError.UNSAFE_CONTENT), viewModel.state.value.phase)
        assertEquals(0, crashlytics.recordExceptionCallCount)
    }

    @Test
    fun `a failed summary is not retried automatically but can be requested`() = runTest {
        useCase.failure = LectureReviewDigestException(LectureReviewDigestError.BUSY)
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(1, useCase.streamCallCount)
        useCase.failure = null
        viewModel.requestSummary(reviews, "Test")
        assertEquals(2, useCase.streamCallCount)
        assertEquals(ReviewDigestPhase.Finished(digest), viewModel.state.value.phase)
    }

    @Test
    fun `stopping mid run resets and the next visit starts again`() = runTest {
        val gate = CompletableDeferred<Unit>()
        useCase.stream = { gate.await() }
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertTrue(viewModel.state.value.phase is ReviewDigestPhase.Writing)
        viewModel.stop()
        assertEquals(ReviewDigestPhase.Idle, viewModel.state.value.phase)
        useCase.stream = null
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        assertEquals(2, useCase.streamCallCount)
        assertEquals(ReviewDigestPhase.Finished(digest), viewModel.state.value.phase)
    }

    @Test
    fun `stopping a finished summary keeps it`() = runTest {
        viewModel.checkAvailability()
        viewModel.summariseIfNeeded(reviews, automatically = true, screen = "Test")
        viewModel.stop()
        assertEquals(ReviewDigestPhase.Finished(digest), viewModel.state.value.phase)
    }
}
