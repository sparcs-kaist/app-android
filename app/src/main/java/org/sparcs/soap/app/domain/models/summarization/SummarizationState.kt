package org.sparcs.soap.app.domain.models.summarization

sealed interface SummarizationState {
    data object Idle : SummarizationState
    data object Loading : SummarizationState
    data class Summarized(val summary: String) : SummarizationState
    data object TooShort : SummarizationState
    data object Unavailable : SummarizationState
    data object Failed : SummarizationState
}
