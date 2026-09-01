package org.sparcs.soap.buddyPreviewSupport.otl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.summarization.SummarizationState
import org.sparcs.soap.app.domain.models.translation.TranslationState
import org.sparcs.soap.app.features.course.CourseViewModel
import org.sparcs.soap.app.features.course.CourseViewModelProtocol

class PreviewCourseViewModel(initialState: CourseViewModel.ViewState) : CourseViewModelProtocol {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<CourseViewModel.ViewState> = _state.asStateFlow()

    override val translationState: StateFlow<TranslationState> = MutableStateFlow(TranslationState.Idle)
    override val summarizationState: StateFlow<SummarizationState> = MutableStateFlow(SummarizationState.Idle)
    override val commentTranslations: StateFlow<Map<String, TranslationState>> = MutableStateFlow(emptyMap())

    override fun translationLanguages(): List<String> = emptyList()
    override fun suggestedTranslationLanguages(): List<String> = emptyList()
    override fun defaultTranslationLanguage(): String = "en"

    override fun translate(content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun showOriginal() {}
    override fun summarize(content: String, scope: CoroutineScope) {}
    override fun hideSummary() {}
    override fun translateComment(commentId: String, content: String, targetLanguage: String, allowDownload: Boolean, scope: CoroutineScope) {}
    override fun showCommentOriginal(commentId: String) {}

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