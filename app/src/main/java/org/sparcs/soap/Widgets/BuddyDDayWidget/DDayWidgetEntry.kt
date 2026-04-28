package org.sparcs.soap.Widgets.BuddyDDayWidget

import kotlinx.serialization.Serializable

@Serializable
enum class DDayType {
    START_OF_SEMESTER,
    END_OF_SEMESTER,
    ERROR
}

@Serializable
data class DDayWidgetEntry(
    val semesterLabel: String,
    val type: DDayType,
    val days: Int,
    val progress: Float,
)

