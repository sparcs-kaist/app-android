package org.sparcs.soap.widgets.buddyDDayWidget

import kotlinx.serialization.Serializable

@Serializable
data class BuddyDDayUiState(
    val entry: DDayWidgetEntry? = null,
    val lastUpdated: Long = 0L,
    val isLoading: Boolean = false,
)

fun DDayWidgetEntry.toDDayWidgetUiState(): BuddyDDayUiState {
    return BuddyDDayUiState(
        entry = this,
        lastUpdated = System.currentTimeMillis()
    )
}

