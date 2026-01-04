package org.sparcs.Widgets.BuddyUpcomingClassWidget

import kotlinx.serialization.Serializable

@Serializable
data class UpcomingClassUiState(
    val entry: WidgetLectureEntry? = null,
    val signInRequired: Boolean = false
)