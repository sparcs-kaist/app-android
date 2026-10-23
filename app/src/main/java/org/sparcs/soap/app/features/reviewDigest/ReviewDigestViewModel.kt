package org.sparcs.soap.app.features.reviewDigest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.otl.LectureReview
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigest
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestError
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestException
import org.sparcs.soap.app.domain.models.reviewDigest.LectureReviewDigestProgress
import org.sparcs.soap.app.domain.services.AnalyticsServiceProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.domain.usecases.reviewDigest.LectureReviewBatching
import org.sparcs.soap.app.domain.usecases.reviewDigest.ReviewDigestUseCaseProtocol
import javax.inject.Inject

sealed interface ReviewDigestPhase {
    data object Idle : ReviewDigestPhase
    data class Reading(val completed: Int, val total: Int) : ReviewDigestPhase
    data class Writing(val digest: LectureReviewDigest) : ReviewDigestPhase
    data class Finished(val digest: LectureReviewDigest) : ReviewDigestPhase
    data class Failed(val reason: LectureReviewDigestError) : ReviewDigestPhase
}

data class ReviewDigestState(
    val phase: ReviewDigestPhase = ReviewDigestPhase.Idle,
    val isAvailable: Boolean = false,
    val reviewCount: Int = 0,
)

interface ReviewDigestViewModelProtocol {
    val state: StateFlow<ReviewDigestState>
    fun canSummarise(reviews: List<LectureReview>): Boolean
    fun checkAvailability()
    fun summariseIfNeeded(reviews: List<LectureReview>, automatically: Boolean, screen: String)
    fun requestSummary(reviews: List<LectureReview>, screen: String)
    fun stop()
}

@HiltViewModel
class ReviewDigestViewModel @Inject constructor(
    private val useCase: ReviewDigestUseCaseProtocol,
    private val analyticsService: AnalyticsServiceProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol,
) : ViewModel(), ReviewDigestViewModelProtocol {
    private val mutableState = MutableStateFlow(ReviewDigestState())
    override val state: StateFlow<ReviewDigestState> = mutableState.asStateFlow()

    private var reviewIDs: List<Int> = emptyList()
    private var requestedReviewIDs: List<Int>? = null
    private var job: Job? = null
    private var availabilityChecked = false

    override fun canSummarise(reviews: List<LectureReview>): Boolean =
        state.value.isAvailable && LectureReviewBatching.readableCount(reviews) >= MINIMUM_REVIEW_COUNT

    override fun checkAvailability() {
        if (availabilityChecked) return
        availabilityChecked = true
        viewModelScope.launch {
            val available = useCase.isAvailable()
            mutableState.update { it.copy(isAvailable = available) }
        }
    }

    override fun summariseIfNeeded(reviews: List<LectureReview>, automatically: Boolean, screen: String) {
        if (!canSummarise(reviews)) return
        val ids = reviews.map { it.id }
        if (!automatically && requestedReviewIDs != ids) return
        when (state.value.phase) {
            is ReviewDigestPhase.Finished, is ReviewDigestPhase.Failed -> if (ids == reviewIDs) return
            is ReviewDigestPhase.Reading, is ReviewDigestPhase.Writing -> if (ids == reviewIDs && job?.isActive == true) return
            ReviewDigestPhase.Idle -> Unit
        }
        job?.cancel()
        reviewIDs = ids
        val count = LectureReviewBatching.readableCount(reviews)
        mutableState.update { it.copy(phase = ReviewDigestPhase.Reading(0, 0), reviewCount = count) }
        analyticsService.logEvent(ReviewDigestEvent.Requested(screen, count))
        job = viewModelScope.launch { run(reviews, screen) }
    }

    override fun requestSummary(reviews: List<LectureReview>, screen: String) {
        requestedReviewIDs = reviews.map { it.id }
        reviewIDs = emptyList()
        job?.cancel()
        mutableState.update { it.copy(phase = ReviewDigestPhase.Idle) }
        summariseIfNeeded(reviews, automatically = false, screen = screen)
    }

    override fun stop() {
        if (job?.isActive != true) return
        job?.cancel()
        reviewIDs = emptyList()
        mutableState.update { it.copy(phase = ReviewDigestPhase.Idle) }
    }

    private suspend fun run(reviews: List<LectureReview>, screen: String) {
        var digest: LectureReviewDigest? = null
        try {
            useCase.streamDigest(reviews).collect { update ->
                when (update) {
                    is LectureReviewDigestProgress.Started -> mutableState.update { it.copy(reviewCount = update.reviewCount) }
                    is LectureReviewDigestProgress.Reading -> mutableState.update {
                        it.copy(phase = ReviewDigestPhase.Reading(update.completed, update.total))
                    }
                    is LectureReviewDigestProgress.Writing -> {
                        digest = update.digest
                        mutableState.update { it.copy(phase = ReviewDigestPhase.Writing(update.digest)) }
                    }
                }
            }
            val result = digest?.takeIf { !it.overview.isNullOrBlank() }
                ?: throw LectureReviewDigestException(LectureReviewDigestError.FAILED)
            mutableState.update { it.copy(phase = ReviewDigestPhase.Finished(result)) }
            analyticsService.logEvent(ReviewDigestEvent.Finished(screen))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            val reason = (error as? LectureReviewDigestException)?.reason ?: LectureReviewDigestError.FAILED
            if (reason == LectureReviewDigestError.FAILED) crashlyticsService.recordException(error)
            mutableState.update { it.copy(phase = ReviewDigestPhase.Failed(reason)) }
            analyticsService.logEvent(ReviewDigestEvent.Failed(screen, reason))
        }
    }

    companion object {
        const val MINIMUM_REVIEW_COUNT = 3
    }
}
