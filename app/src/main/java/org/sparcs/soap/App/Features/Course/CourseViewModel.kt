package org.sparcs.soap.App.Features.Course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage
import org.sparcs.soap.App.Domain.Services.AnalyticsServiceProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.CourseUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.OTL.ReviewUseCaseProtocol
import org.sparcs.soap.App.Features.Course.Event.CourseViewEvent
import javax.inject.Inject

interface CourseViewModelProtocol {
    val state: StateFlow<CourseViewModel.ViewState>
    fun loadCourse()
    fun toggleReviewLike(review: LectureReview)
}

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseUseCase: CourseUseCaseProtocol,
    private val reviewUseCase: ReviewUseCaseProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), CourseViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(
            val course: Course,
            val reviews: List<LectureReview>,
            val writtenReview: LectureReview?,
            val reviewPage: LectureReviewPage
        ) : ViewState()

        data class Error(val message: String) : ViewState()
    }

    private val courseId: Int? = savedStateHandle.get<String>("courseId")?.toIntOrNull()

    // MARK: - State
    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state = _state.asStateFlow()

    init {
        loadCourse()
    }

    override fun loadCourse() {
        val id = courseId ?: return
        viewModelScope.launch {
            try {
                _state.value = ViewState.Loading

                val course = courseUseCase.getCourse(id)
                analyticsService.logEvent(CourseViewEvent.CourseLoaded)

                fetchReviews(id, course)

            } catch (e: Exception) {
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error(e.localizedMessage ?: "Failed")
            }
        }
    }

    private fun fetchReviews(id: Int, course: Course) {
        viewModelScope.launch {
            try {
                val allReviewsDeferred = async { reviewUseCase.fetchReviews(id, null, 0, 100) }
                val myTotalReviewsDeferred = async { reviewUseCase.getWrittenReviews() }

                val allReviewsResult = allReviewsDeferred.await()
                val myTotalReviews = myTotalReviewsDeferred.await()

                splitMyReviewFromOthers(
                    allReviews = allReviewsResult.reviews,
                    myTotalReviews = myTotalReviews,
                    currentCourseId = id,
                    course = course,
                    reviewPage = allReviewsResult
                )

                analyticsService.logEvent(CourseViewEvent.ReviewsLoaded)
            } catch (e: Exception) {
                crashlyticsService.recordException(e)
                _state.value = ViewState.Error("Reviews load failed")
            }
        }
    }

    private fun splitMyReviewFromOthers(
        allReviews: List<LectureReview>,
        myTotalReviews: List<LectureReview>,
        currentCourseId: Int,
        course: Course,
        reviewPage: LectureReviewPage
    ) {
        val myReview = myTotalReviews.find { it.courseID == currentCourseId }

        _state.value = ViewState.Loaded(
            course = course,
            reviews = if (myReview != null) allReviews.filter { it.id != myReview.id } else allReviews,
            writtenReview = myReview,
            reviewPage = reviewPage
        )
    }

    override fun toggleReviewLike(review: LectureReview) {
        val currentState = _state.value as? ViewState.Loaded ?: return

        val isCurrentlyLiked = review.likedByUser
        val updatedReviews = currentState.reviews.map { target ->
            if (target.id == review.id) {
                val nextLikeCount = if (isCurrentlyLiked) target.like - 1 else target.like + 1
                target.copy(
                    likedByUser = !isCurrentlyLiked,
                    like = nextLikeCount
                )
            } else {
                target
            }
        }

        _state.value = currentState.copy(reviews = updatedReviews.toList())

        viewModelScope.launch {
            try {
                reviewUseCase.likeReview(review.id, !isCurrentlyLiked)
                analyticsService.logEvent(CourseViewEvent.LikeReview)
            } catch (e: Exception) {
                _state.value = currentState
                crashlyticsService.recordException(e)
            }
        }
    }
}