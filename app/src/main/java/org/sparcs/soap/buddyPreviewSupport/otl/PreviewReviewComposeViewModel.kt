package org.sparcs.soap.buddyPreviewSupport.otl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.helpers.AlertState
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.features.reviewCompose.ReviewComposeViewModelProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock

class PreviewReviewComposeViewModel(
    initialReview: LectureReview? = null,
) : ReviewComposeViewModelProtocol {

    override val lecture: Lecture = Lecture.mock()

    private val _writtenReview = MutableStateFlow(initialReview)
    override val writtenReview: StateFlow<LectureReview?> = _writtenReview.asStateFlow()

    override var alertState: AlertState? = null
    override var isAlertPresented: Boolean = false
    override var isUploading: Boolean = false

    override suspend fun submitReview(content: String, grade: Int, load: Int, speech: Int): Boolean {
        isUploading = true
        return true
    }

    override fun handleException(error: Throwable) {}

}

