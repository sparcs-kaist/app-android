package org.sparcs.soap.widgets.buddyUpcomingClassWidget

import kotlinx.serialization.Serializable

@Serializable
data class UpcomingClassUiState(
    val entry: WidgetLectureEntry? = null,
    val signInRequired: Boolean = false,
    val isLoading: Boolean = false
)

fun WidgetLectureEntry.toUpcomingWidgetUiState(): UpcomingClassUiState {
    return UpcomingClassUiState(
        entry = this,
        signInRequired = signInRequired
    )
}
