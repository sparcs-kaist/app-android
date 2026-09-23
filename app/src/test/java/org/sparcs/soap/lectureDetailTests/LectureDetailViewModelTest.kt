package org.sparcs.soap.lectureDetailTests

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.NetworkErrorMapper
import org.sparcs.soap.app.shared.viewModels.TextProcessingDelegate
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.translation.PostTranslationResult
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.summarization.SummarizationResultState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockCourseUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockReviewUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockTimetableUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class LectureDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockCourseUseCase: MockCourseUseCase
    private lateinit var mockReviewUseCase: MockReviewUseCase
    private lateinit var mockTimetableUseCase: MockTimetableUseCase
    private lateinit var mockUserUseCase: MockUserUseCase
    private lateinit var viewModel: LectureDetailViewModel

    @Before
    fun setup() {
        mockCourseUseCase = MockCourseUseCase()
        mockReviewUseCase = MockReviewUseCase()
        mockTimetableUseCase = MockTimetableUseCase()
        mockUserUseCase = MockUserUseCase()
    }

    private fun createViewModel(lecture: Lecture = Lecture.mock()) {
        val savedStateHandle = SavedStateHandle(mapOf("lecture_json" to Gson().toJson(lecture)))
        viewModel = LectureDetailViewModel(
            courseUseCase = mockCourseUseCase,
            reviewUseCase = mockReviewUseCase,
            timetableUseCase = mockTimetableUseCase,
            userUseCase = mockUserUseCase,
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            textProcessingDelegate = TextProcessingDelegate(
                object : PostTranslationUseCaseProtocol {
                    override fun availableLanguages() = emptyList<String>()
                    override fun suggestedLanguages() = emptyList<String>()
                    override fun deviceLanguage() = "en"
                    override suspend fun translate(text: String, targetLanguage: String, isHtml: Boolean, allowDownload: Boolean) = PostTranslationResult.Unsupported
                },
                object : SummarizationUseCaseProtocol {
                    override suspend fun isAvailable() = false
                    override suspend fun summarise(text: String, isHtml: Boolean) = SummarizationResultState.Unavailable
                },
            ),
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `init loads course and reviews into loaded state`() = runTest {
        val course = Course.mock()
        mockCourseUseCase.getCourseResult = Result.success(course)
        mockReviewUseCase.fetchReviewsResult =
            Result.success(LectureReviewPage.mock().copy(reviews = LectureReview.mockList()))
        mockReviewUseCase.writtenReviewsResult = Result.success(emptyList())

        createViewModel()

        assertEquals(LectureDetailViewModel.ViewState.Loaded, viewModel.state.value)
        assertEquals(course, viewModel.course.value)
        assertTrue(viewModel.reviews.value.isNotEmpty())
    }

    @Test
    fun `toggleReviewLike optimistically flips like and count`() = runTest {
        val review = LectureReview.mock().copy(id = 88, likedByUser = false, like = 3)
        mockCourseUseCase.getCourseResult = Result.success(Course.mock())
        mockReviewUseCase.fetchReviewsResult =
            Result.success(LectureReviewPage.mock().copy(reviews = listOf(review)))
        mockReviewUseCase.writtenReviewsResult = Result.success(emptyList())
        createViewModel()

        viewModel.toggleReviewLike(review)

        val updated = viewModel.reviews.value.first { it.id == 88 }
        assertTrue(updated.likedByUser)
        assertEquals(4, updated.like)
        assertEquals(1, mockReviewUseCase.likeReviewCallCount)
    }
    @Test fun offlineReviewFailureIsHandledAndRetrySucceeds() = runTest {
        val error = NetworkError.NoConnection()
        mockReviewUseCase.fetchReviewsResult = Result.failure(error)
        createViewModel()
        assertEquals(error, (viewModel.state.value as LectureDetailViewModel.ViewState.Error).error)
        assertEquals(false, viewModel.canWriteReview.value)
        mockReviewUseCase.fetchReviewsResult = Result.success(LectureReviewPage.mock())
        viewModel.fetchReviews(Lecture.mock())
        assertEquals(LectureDetailViewModel.ViewState.Loaded, viewModel.state.value)
    }

    @Test fun writtenReviewFailureIsHandled() = runTest {
        val error = NetworkError.NoConnection()
        mockReviewUseCase.writtenReviewsResult = Result.failure(error)
        createViewModel()
        assertEquals(error, (viewModel.state.value as LectureDetailViewModel.ViewState.Error).error)
    }

    @Test fun cancellationIsNotMappedToNetworkFailure() {
        val cancellation = CancellationException("Screen closed")
        val result = runCatching { NetworkErrorMapper.map(cancellation) }
        assertTrue(result.exceptionOrNull() === cancellation)
    }

}
