package org.sparcs.soap.widgets.buddyTimetableWidget

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.backgroundColor
import org.sparcs.soap.app.domain.models.otl.textColor
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry

@Serializable
data class TimetableUiState(
    val signInRequired: Boolean = true,
    val timetable: WidgetTimetableEntry? = null,
    val lastUpdated: Long = 0L,
    val isLoading: Boolean = false,
)

fun Timetable.toWidgetUiState(): TimetableUiState {
    val times = lectures.flatMap { it.classes }.filter { it.end > it.begin }
    val validActivities = activities.filter { it.day in 0..6 && it.begin >= 0 && it.end <= 1440 && it.end > it.begin }
    val begins = times.map { it.begin } + validActivities.map { it.begin }
    val ends = times.map { it.end } + validActivities.map { it.end }
    val calculatedMin = (begins.minOrNull() ?: 540) / 60 * 60
    val calculatedMax = (ends.maxOrNull()?.let { ((it + 59) / 60) * 60 } ?: 1080).coerceAtLeast(calculatedMin + 60)

    val widgetItems = this.lectures.flatMap { lecture ->
        lecture.classes.filter { it.end > it.begin }.map { ct ->
            WidgetLectureEntry(
                title = lecture.name + lecture.subtitle,
                classroom = "(${ct.buildingCode}) ${ct.roomName}",
                day = ct.day,
                startMinutes = ct.begin,
                durationMinutes = ct.end - ct.begin,
                bgColor = String.format("#%06X", (0xFFFFFF and lecture.backgroundColor.toArgb())),
                textColor = String.format("#%06X", (0xFFFFFF and textColor.toArgb())),
                signInRequired = false
            )
        }
    }

    val activityItems = validActivities.map { activity ->
        WidgetLectureEntry(
            title = activity.title,
            classroom = activity.location,
            day = DayType.fromValue(activity.day),
            startMinutes = activity.begin,
            durationMinutes = activity.end - activity.begin,
            bgColor = String.format("#%06X", 0xFFFFFF and activity.backgroundColor.toArgb()),
            textColor = String.format("#%06X", 0xFFFFFF and textColor.toArgb()),
            signInRequired = false,
            activityID = activity.id
        )
    }

    return TimetableUiState(
        signInRequired = false,
        timetable = WidgetTimetableEntry(
            lecturesByDay = widgetItems.groupBy { it.day },
            visibleDays = (DayType.weekdays() + (widgetItems + activityItems).mapNotNull { it.day }).distinct().sortedBy { it.value },
            activitiesByDay = activityItems.groupBy { it.day!! },
            minMinutes = calculatedMin,
            maxMinutes = calculatedMax
        ),
        lastUpdated = System.currentTimeMillis()
    )
}