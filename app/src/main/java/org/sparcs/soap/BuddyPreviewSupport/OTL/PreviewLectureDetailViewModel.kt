package org.sparcs.soap.BuddyPreviewSupport.OTL

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.OTL.Course
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.OTL.mock
import org.sparcs.soap.App.Shared.Mocks.OTL.mockList

class PreviewLectureDetailViewModel(initialState: LectureDetailViewModel.ViewState) :
    LectureDetailViewModelProtocol {

    private val _lecture = MutableStateFlow(Lecture.mock())
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _course = MutableStateFlow<Course?>(Course.mockList()[0])
    override val course: StateFlow<Course?> = _course.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureDetailViewModel.ViewState> = _state.asStateFlow()

    private val _reviews = MutableStateFlow<List<LectureReview>>(LectureReview.mockList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews.asStateFlow()

    private val _writtenReview = MutableStateFlow<LectureReview?>(null)
    override val writtenReview: StateFlow<LectureReview?> = _writtenReview.asStateFlow()

    private val _canWriteReview = MutableStateFlow(false)
    override val canWriteReview: StateFlow<Boolean> = _canWriteReview.asStateFlow()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override fun fetchCourse(courseID: Int) {}
    override fun fetchReviews(lecture: Lecture) {}

    override fun toggleReviewLike(review: LectureReview) {
        val currentList = _reviews.value
        val updatedList = currentList.map {
            if (it.id == review.id) {
                val newLiked = !it.likedByUser
                it.copy(
                    likedByUser = newLiked,
                    like = if (newLiked) it.like + 1 else it.like - 1
                )
            } else it
        }
        _reviews.value = updatedList
    }

    override fun updateWrittenReview(newReview: LectureReview) {}
}