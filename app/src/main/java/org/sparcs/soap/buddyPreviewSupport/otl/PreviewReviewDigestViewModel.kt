package org.sparcs.soap.buddyPreviewSupport.otl

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestPhase
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestState
import org.sparcs.soap.app.features.reviewDigest.ReviewDigestViewModelProtocol

class PreviewReviewDigestViewModel(
    initialState: ReviewDigestState = ReviewDigestState(
        phase = ReviewDigestPhase.Finished(
            LectureReviewDigest(
                overview = "Clear lectures and fair exams, but weekly assignments take time.",
                liked = listOf("Clear explanations", "Helpful TAs"),
                disliked = listOf("Heavy weekly homework"),
                workload = "Weekly problem sets and two exams.",
                grading = "Generous if you submit everything.",
            )
        ),
        isAvailable = true,
        reviewCount = 3,
    ),
) : ReviewDigestViewModelProtocol {
    private val mutableState = MutableStateFlow(initialState)
    override val state: StateFlow<ReviewDigestState> = mutableState.asStateFlow()

    override fun canSummarise(reviews: List<LectureReview>): Boolean = state.value.isAvailable
    override fun checkAvailability() = Unit
    override fun summariseIfNeeded(reviews: List<LectureReview>, automatically: Boolean, screen: String) = Unit
    override fun requestSummary(reviews: List<LectureReview>, screen: String) = Unit
    override fun stop() = Unit
}
