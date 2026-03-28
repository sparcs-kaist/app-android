package org.sparcs.soap.Widgets.BuddyTimetableWidget

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.backgroundColor
import org.sparcs.soap.App.Domain.Models.OTL.textColor
import org.sparcs.soap.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry

@Serializable
data class TimetableUiState(
    val signInRequired: Boolean = true,
    val timetable: WidgetTimetableEntry? = null,
    val lastUpdated: Long = 0L,
)

fun Timetable.toWidgetUiState(): TimetableUiState {
    val times = this.lectures.flatMap { it.classes }

    val calculatedMin = times.minOfOrNull { it.begin }?.let { (it / 60) * 60 } ?: (9 * 60)
    val calculatedMax = times.maxOfOrNull { it.end }?.let { ((it / 60) + 1) * 60 } ?: (18 * 60)
    val visibleDays = times.map { it.day }.distinct().sorted()

    val widgetItems = this.lectures.flatMap { lecture ->
        lecture.classes.map { ct ->
            WidgetLectureEntry(
                title = lecture.name + lecture.subtitle,
                classroom = "(${ct.buildingCode}) ${ct.roomName}",
                day = ct.day,
                startMinutes = ct.begin,
                durationMinutes = ct.end - ct.begin,
                bgColor = String.format("#%06X", (0xFFFFFF and lecture.backgroundColor.toArgb())),
                textColor = String.format("#%06X", (0xFFFFFF and lecture.textColor.toArgb())),
                signInRequired = false
            )
        }
    }

    return TimetableUiState(
        signInRequired = false,
        timetable = WidgetTimetableEntry(
            lecturesByDay = widgetItems.groupBy { it.day },
            visibleDays = this.visibleDays,
            minMinutes = calculatedMin,
            maxMinutes = calculatedMax
        ),
        lastUpdated = System.currentTimeMillis()
    )
}