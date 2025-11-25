package com.sparcs.soap.Shared.ViewModelMocks.OTL

import com.sparcs.soap.Domain.Models.OTL.Lecture
import com.sparcs.soap.Domain.Models.OTL.LectureReview
import com.sparcs.soap.Features.LectureDetail.LectureDetailViewModel
import com.sparcs.soap.Features.LectureDetail.LectureDetailViewModelProtocol
import com.sparcs.soap.Shared.Mocks.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class MockLectureDetailViewModel(initialState: LectureDetailViewModel.ViewState) : LectureDetailViewModelProtocol {

    private val _lecture = MutableStateFlow(Lecture.mock())
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureDetailViewModel.ViewState> = _state.asStateFlow()

    private val _reviews = MutableStateFlow<List<LectureReview>>(emptyList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews.asStateFlow()

    override var isInCurrentTimetable: Boolean = false
    override fun fetchReviews(lectureID: Int) {}
    override fun writeReview(review: LectureReview) {}
}