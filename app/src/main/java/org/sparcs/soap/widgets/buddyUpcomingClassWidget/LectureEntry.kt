package org.sparcs.soap.widgets.buddyUpcomingClassWidget

import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.enums.otl.DayType
import java.util.Locale

@Serializable
data class WidgetLectureEntry(
    val title: String?,
    val classroom: String?,
    val day: DayType?,
    val signInRequired: Boolean,
    val startMinutes: Int?,
    val durationMinutes: Int?,
    val bgColor: String = "#FFFFFF",
    val textColor: String = "#000000"
) {
    companion object

    val formattedTimeRange: String
        get() {
            val start = startMinutes ?: 0
            val duration = durationMinutes ?: 0
            val end = start + duration

            fun Int.toTime(): String = String.format(Locale.ROOT,  "%02d:%02d", (this / 60) % 24, this % 60)

            return "${start.toTime()} - ${end.toTime()}"
        }
}

fun WidgetLectureEntry.Companion.mock(): WidgetLectureEntry {
    return WidgetLectureEntry(
        title = "소프트웨어 공학",
        classroom = "정보전자공학동 101",
        day = DayType.MON,
        startMinutes = 540,
        durationMinutes = 75,
        bgColor = "#4A90E2",
        textColor = "#FFFFFF",
        signInRequired = false
    )
}

fun WidgetLectureEntry.Companion.empty(signInRequired: Boolean) = WidgetLectureEntry(
    title = null,
    classroom = null,
    day = null,
    startMinutes = null,
    durationMinutes = null,
    signInRequired = signInRequired
)