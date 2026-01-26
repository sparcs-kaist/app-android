package org.sparcs.soap.App.Shared.ViewModelMocks.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.mock


class MockLectureDetailViewModel(initialState: LectureDetailViewModel.ViewState) :
    LectureDetailViewModelProtocol {

    private val _lecture = MutableStateFlow(Lecture.mock())
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureDetailViewModel.ViewState> = _state.asStateFlow()

    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews.asStateFlow()

    override var isInCurrentTimetable: Boolean = false
    override fun fetchReviews(lectureID: Int) {}
    override fun writeReview(lectureID: Int, review: LectureReview) {}
}