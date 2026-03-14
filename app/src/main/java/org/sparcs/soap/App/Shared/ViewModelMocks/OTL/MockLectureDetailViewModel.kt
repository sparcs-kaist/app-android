package org.sparcs.soap.App.Shared.ViewModelMocks.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.Review
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.soap.App.Features.LectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.mock


class MockLectureDetailViewModel(initialState: LectureDetailViewModel.ViewState) :
    LectureDetailViewModelProtocol {

    private val _lecture = MutableStateFlow(Lecture.mock())
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureDetailViewModel.ViewState> = _state.asStateFlow()

    private val _reviews = MutableStateFlow<ReviewResponse>(ReviewResponse(emptyList(), 0.0, 0.0, 0.0))
    override val reviews: StateFlow<ReviewResponse> = _reviews.asStateFlow()

    private val _canWriteReview = MutableStateFlow(true)
    override val canWriteReview: StateFlow<Boolean> = _canWriteReview.asStateFlow()

    private val _writtenReview = MutableStateFlow<Review?>(null)
    override val writtenReview: StateFlow<Review?> = _writtenReview.asStateFlow()

    override var isInCurrentTimetable: Boolean = false
    override fun fetchReviews() {}
    override fun writeReview(content: String, grade: Int, load: Int, speech: Int, editing: Boolean) {}
}