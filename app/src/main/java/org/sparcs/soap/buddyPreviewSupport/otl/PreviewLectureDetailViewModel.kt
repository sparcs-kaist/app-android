package org.sparcs.soap.buddyPreviewSupport.otl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.Course
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModelProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList

class PreviewLectureDetailViewModel(initialState: LectureDetailViewModel.ViewState) :
    LectureDetailViewModelProtocol {

    private val _lecture = MutableStateFlow(Lecture.mock())
    override val lecture: StateFlow<Lecture> = _lecture.asStateFlow()

    private val _course = MutableStateFlow<Course?>(Course.mockList()[0])
    override val course: StateFlow<Course?> = _course.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LectureDetailViewModel.ViewState> = _state.asStateFlow()

    private val _reviews = MutableStateFlow(LectureReview.mockList())
    override val reviews: StateFlow<List<LectureReview>> = _reviews.asStateFlow()

    private val _writtenReview = MutableStateFlow<LectureReview?>(null)
    override val writtenReview: StateFlow<LectureReview?> = _writtenReview.asStateFlow()

    private val _canWriteReview = MutableStateFlow(false)
    override val canWriteReview: StateFlow<Boolean> = _canWriteReview.asStateFlow()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    override val translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    override val summarizationState = MutableStateFlow<SummarizationState>(SummarizationState.Idle)

    override fun translationLanguages(): List<String> = listOf("ko", "en")
    override fun suggestedTranslationLanguages(): List<String> = listOf("ko", "en")
    override fun defaultTranslationLanguage(): String = "ko"

    override fun translateReview(content: String, targetLanguage: String, allowDownload: Boolean) {}
    override fun showOriginal() {}
    override fun summarizeReview(content: String) {}
    override fun hideSummary() {}

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