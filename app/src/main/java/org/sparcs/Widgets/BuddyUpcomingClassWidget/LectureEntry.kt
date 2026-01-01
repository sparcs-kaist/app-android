package org.sparcs.Widgets.BuddyUpcomingClassWidget

import kotlinx.serialization.Serializable
import org.sparcs.App.Domain.Enums.OTL.DayType

@Serializable
data class WidgetLectureEntry(
    val title: String,
    val classroom: String,
    val day: DayType,
    val startMinutes: Int,
    val durationMinutes: Int,
    val bgColor: String,
    val textColor: String
)