package org.sparcs.soap.BuddyPreviewSupport.OTL

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Features.ReviewCompose.ReviewComposeViewModelProtocol
import org.sparcs.soap.App.Shared.Mocks.OTL.mock

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

