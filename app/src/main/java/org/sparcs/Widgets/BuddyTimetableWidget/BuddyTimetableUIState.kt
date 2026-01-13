package org.sparcs.Widgets.BuddyTimetableWidget

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Domain.Models.OTL.backgroundColor
import org.sparcs.App.Domain.Models.OTL.textColor
import org.sparcs.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry

@Serializable
data class TimetableUiState(
    val signInRequired: Boolean = true,
    val timetable: WidgetTimetableEntry? = null,
    val lastUpdated: Long = 0L,
)

fun Timetable.toWidgetUiState(): TimetableUiState {
    val times = this.lectures.flatMap { it.classTimes }
    val calculatedMin = times.minOfOrNull { it.begin }?.let { (it / 60) * 60 }
        ?: this.minMinutes
    val calculatedMax = times.maxOfOrNull { it.end }?.let { ((it / 60) + 1) * 60 }
        ?: this.gappedMaxMinutes

    val widgetItems = this.lectures.flatMap { lecture ->
        lecture.classTimes.map { ct ->
            WidgetLectureEntry(
                title = lecture.title.localized(),
                classroom = ct.classroomNameShort.localized(),
                day = ct.day,
                startMinutes = ct.begin,
                durationMinutes = ct.end - ct.begin,
                bgColor = "#" + Integer.toHexString(lecture.backgroundColor.toArgb()).uppercase(),
                textColor = "#" + Integer.toHexString(lecture.textColor.toArgb()).uppercase(),
                signInRequired = false
            )
        }
    }
    val lecturesByDay = widgetItems.groupBy { it.day }

    return TimetableUiState(
        signInRequired = false,
        timetable = WidgetTimetableEntry(
            lecturesByDay = lecturesByDay,
            visibleDays = this.visibleDays,
            minMinutes = calculatedMin,
            maxMinutes = calculatedMax
        ),
        lastUpdated = System.currentTimeMillis()
    )
}
