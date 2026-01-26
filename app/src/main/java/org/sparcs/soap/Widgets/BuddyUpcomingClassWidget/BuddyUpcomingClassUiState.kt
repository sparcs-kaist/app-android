package org.sparcs.soap.Widgets.BuddyUpcomingClassWidget

import kotlinx.serialization.Serializable

@Serializable
data class UpcomingClassUiState(
    val entry: WidgetLectureEntry? = null,
    val signInRequired: Boolean = false
)

fun WidgetLectureEntry.toUpcomingWidgetUiState(): UpcomingClassUiState {
    return UpcomingClassUiState(
        entry = this,
        signInRequired = signInRequired
    )
}
