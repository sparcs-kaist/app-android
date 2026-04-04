package org.sparcs.soap.BuddyPreviewSupport.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Features.Course.CourseViewModel
import org.sparcs.soap.App.Features.Course.CourseViewModelProtocol

class PreviewCourseViewModel(initialState: CourseViewModel.ViewState) : CourseViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<CourseViewModel.ViewState> = _state.asStateFlow()

    override val alertState: AlertState? = null
    override var isAlertPresented: Boolean = false

    override fun loadCourse() {}

    override fun toggleReviewLike(review: LectureReview) {
        val currentState = _state.value
        if (currentState is CourseViewModel.ViewState.Loaded) {
            val updatedReviews = currentState.reviews.map {
                if (it.id == review.id) {
                    val newLiked = !it.likedByUser
                    it.copy(
                        likedByUser = newLiked,
                        like = if (newLiked) it.like + 1 else it.like - 1
                    )
                } else it
            }
            _state.value = currentState.copy(reviews = updatedReviews)
        }
    }
}