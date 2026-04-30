package org.sparcs.soap.Widgets.BuddyDDayWidget

import kotlinx.serialization.Serializable

@Serializable
data class BuddyDDayUiState(
    val entry: DDayWidgetEntry? = null,
    val signInRequired: Boolean = false,
    val lastUpdated: Long = 0L,
)

fun DDayWidgetEntry.toDDayWidgetUiState(): BuddyDDayUiState {
    return BuddyDDayUiState(
        entry = this,
        signInRequired = false,
        lastUpdated = System.currentTimeMillis()
    )
}

