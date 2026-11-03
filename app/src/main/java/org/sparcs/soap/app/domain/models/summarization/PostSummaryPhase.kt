package org.sparcs.soap.app.domain.models.summarization

import org.sparcs.soap.app.domain.usecases.summarization.ReviewSummaryError

sealed interface PostSummaryPhase {
    data class Summarising(val partial: String) : PostSummaryPhase
    data class Finished(val summary: String) : PostSummaryPhase
    data class Failed(val reason: ReviewSummaryError) : PostSummaryPhase

    val isShown: Boolean
        get() = this is Summarising || this is Finished
}
