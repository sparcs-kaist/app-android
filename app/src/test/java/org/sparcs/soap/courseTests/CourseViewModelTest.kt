package org.sparcs.soap.courseTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.features.course.CourseViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockCourseUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class CourseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockCourseUseCase: MockCourseUseCase
    private lateinit var mockReviewUseCase: MockReviewUseCase
    private lateinit var viewModel: CourseViewModel

    @Before
    fun setup() {
        mockCourseUseCase = MockCourseUseCase()
        mockReviewUseCase = MockReviewUseCase()
    }

    private fun createViewModel(courseId: String? = "1") {
        val savedStateHandle = SavedStateHandle(
            if (courseId != null) mapOf("courseId" to courseId) else emptyMap()
        )
        viewModel = CourseViewModel(
            courseUseCase = mockCourseUseCase,
            reviewUseCase = mockReviewUseCase,
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `loadCourse success sets loaded state`() = runTest {
        val course = Course.mock()
        mockCourseUseCase.getCourseResult = Result.success(course)
        mockReviewUseCase.fetchReviewsResult =
            Result.success(LectureReviewPage.mock().copy(reviews = LectureReview.mockList()))
        mockReviewUseCase.writtenReviewsResult = Result.success(emptyList())

        createViewModel("1")

        val state = viewModel.state.value
        assertTrue(state is CourseViewModel.ViewState.Loaded)
        state as CourseViewModel.ViewState.Loaded
        assertEquals(course, state.course)
        assertEquals(1, mockCourseUseCase.getCourseCallCount)
    }

    @Test
    fun `loadCourse failure sets error state`() = runTest {
        mockCourseUseCase.getCourseResult = Result.failure(Exception("Test failure"))

        createViewModel("1")

        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Error)
    }

    @Test
    fun `missing courseId keeps loading and does not fetch`() = runTest {
        createViewModel(courseId = null)

        assertEquals(CourseViewModel.ViewState.Loading, viewModel.state.value)
        assertEquals(0, mockCourseUseCase.getCourseCallCount)
    }

    @Test
    fun `toggleReviewLike optimistically flips like and count`() = runTest {
        val review = LectureReview.mock().copy(id = 77, likedByUser = false, like = 5, courseID = 1)
        mockReviewUseCase.fetchReviewsResult =
            Result.success(LectureReviewPage.mock().copy(reviews = listOf(review)))
        mockReviewUseCase.writtenReviewsResult = Result.success(emptyList())
        createViewModel("1")

        viewModel.toggleReviewLike(review)

        val state = viewModel.state.value as CourseViewModel.ViewState.Loaded
        val updated = state.reviews.first { it.id == 77 }
        assertTrue(updated.likedByUser)
        assertEquals(6, updated.like)
        assertEquals(1, mockReviewUseCase.likeReviewCallCount)
        assertEquals(77, mockReviewUseCase.lastLikedReviewId)
        assertEquals(true, mockReviewUseCase.lastLikeValue)
    }
}
