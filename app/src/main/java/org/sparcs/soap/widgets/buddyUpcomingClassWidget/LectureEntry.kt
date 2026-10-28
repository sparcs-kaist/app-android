package org.sparcs.soap.widgets.buddyUpcomingClassWidget

import android.content.Context
import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.shared.formatters.formatTime
import org.sparcs.soap.app.shared.formatters.formatTimeRange

@Serializable
data class WidgetLectureEntry(
    val title: String?,
    val classroom: String?,
    val day: DayType?,
    val signInRequired: Boolean,
    val startMinutes: Int?,
    val durationMinutes: Int?,
    val bgColor: String = "#FFFFFF",
    val textColor: String = "#000000",
    val activityID: Int? = null,
    /** Palette slot this entry occupies, so a theme change recolors it without a network sync. */
    val colorID: Int? = null,
    val subtitle: String? = null,
) {
    companion object

    fun formattedStartTime(context: Context): String = context.formatTime(startMinutes ?: 0)

    fun formattedTimeRange(context: Context): String {
        val start = startMinutes ?: 0
        return context.formatTimeRange(start, start + (durationMinutes ?: 0))
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