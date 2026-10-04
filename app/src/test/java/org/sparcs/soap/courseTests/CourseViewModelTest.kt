package org.sparcs.soap.courseTests

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.domain.usecases.otl.CourseUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.ReviewUseCaseProtocol
import org.sparcs.soap.app.domain.models.otl.Professor
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewCourseViewModel
import org.sparcs.soap.app.features.course.CourseViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockCourseUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
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

    private fun createViewModel(courseId: String? = "1", courses: CourseUseCaseProtocol = mockCourseUseCase, reviews: ReviewUseCaseProtocol = mockReviewUseCase) {
        val savedStateHandle = SavedStateHandle(
            if (courseId != null) mapOf("courseId" to courseId) else emptyMap()
        )
        viewModel = CourseViewModel(
            courseUseCase = courses,
            reviewUseCase = reviews,
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            savedStateHandle = savedStateHandle,
            textProcessingDelegate = PreviewCourseViewModel(CourseViewModel.ViewState.Loading),
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

        assertEquals("Test failure", viewModel.courseError.value?.message)
        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Loaded)
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

    @Test
    fun `slow reviews do not delay course information`() = runTest {
        val gate = CompletableDeferred<LectureReviewPage>()
        val delayed = object : ReviewUseCaseProtocol by mockReviewUseCase {
            override suspend fun fetchReviews(courseID: Int, professorID: Int?, offset: Int, limit: Int) = gate.await()
        }
        createViewModel(reviews = delayed)
        assertEquals(Course.mock().id, viewModel.course.value?.id)
        assertEquals(CourseViewModel.ViewState.Loading, viewModel.state.value)
        gate.complete(LectureReviewPage.mock())
        runCurrent()
        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Loaded)
    }

    @Test
    fun `slow course does not delay reviews`() = runTest {
        val gate = CompletableDeferred<Course>()
        val delayed = object : CourseUseCaseProtocol by mockCourseUseCase {
            override suspend fun getCourse(courseID: Int) = gate.await()
        }
        createViewModel(courses = delayed)
        assertEquals(null, viewModel.course.value)
        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Loaded)
        gate.complete(Course.mock())
        runCurrent()
        assertEquals(Course.mock().id, viewModel.course.value?.id)
    }

    @Test
    fun `review error leaves course visible and retries only reviews`() = runTest {
        mockReviewUseCase.fetchReviewsResult = Result.failure(Exception("reviews failed"))
        createViewModel()
        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Error)
        assertEquals(Course.mock().id, viewModel.course.value?.id)
        mockReviewUseCase.fetchReviewsResult = Result.success(LectureReviewPage.mock())
        viewModel.fetchReviews()
        assertTrue(viewModel.state.value is CourseViewModel.ViewState.Loaded)
        assertEquals(1, mockCourseUseCase.getCourseCallCount)
    }

    @Test
    fun `professor selection filters personal review and reuses written reviews`() = runTest {
        var writtenRequests = 0
        val professorRequests = mutableListOf<Int?>()
        val personal = LectureReview.mock().copy(courseID = 1, professors = listOf(Professor(7, "Seven")))
        val reviews = object : ReviewUseCaseProtocol by mockReviewUseCase {
            override suspend fun getWrittenReviews(): List<LectureReview> { writtenRequests++; return listOf(personal) }
            override suspend fun fetchReviews(courseID: Int, professorID: Int?, offset: Int, limit: Int): LectureReviewPage {
                professorRequests.add(professorID)
                return LectureReviewPage.mock().copy(reviews = emptyList())
            }
        }
        createViewModel(reviews = reviews)
        viewModel.selectProfessor(8)
        assertEquals(null, (viewModel.state.value as CourseViewModel.ViewState.Loaded).writtenReview)
        viewModel.selectProfessor(7)
        assertEquals(personal, (viewModel.state.value as CourseViewModel.ViewState.Loaded).writtenReview)
        viewModel.selectProfessor(7)
        assertEquals(listOf(null, 8, 7), professorRequests)
        assertEquals(1, writtenRequests)
        assertEquals(1, mockCourseUseCase.getCourseCallCount)
    }

    @Test
    fun `late cancelled professor response cannot replace current selection`() = runTest {
        val old = CompletableDeferred<LectureReviewPage>()
        val currentPage = LectureReviewPage.mock().copy(averageGrade = 3.0)
        val reviews = object : ReviewUseCaseProtocol by mockReviewUseCase {
            override suspend fun fetchReviews(courseID: Int, professorID: Int?, offset: Int, limit: Int): LectureReviewPage {
                return if (professorID == 7) withContext(NonCancellable) { old.await() } else currentPage
            }
        }
        createViewModel(reviews = reviews)
        viewModel.selectProfessor(7)
        viewModel.selectProfessor(8)
        old.complete(LectureReviewPage.mock().copy(averageGrade = 1.0))
        runCurrent()
        assertEquals(8, viewModel.selectedProfessorID.value)
        assertEquals(currentPage, (viewModel.state.value as CourseViewModel.ViewState.Loaded).reviewPage)
    }
}
