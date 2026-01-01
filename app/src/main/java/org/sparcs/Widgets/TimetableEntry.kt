package org.sparcs.Widgets

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.Serializable
import org.sparcs.App.Domain.Enums.OTL.DayType
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Domain.Models.OTL.backgroundColor
import org.sparcs.App.Domain.Models.OTL.textColor

@Serializable
data class WidgetTimetable(
    val lecturesByDay: Map<DayType, List<WidgetLectureEntry>>,
    val visibleDays: List<DayType>,
    val minMinutes: Int,
    val maxMinutes: Int
) {
    fun getLectures(dayName: DayType): List<WidgetLectureEntry> {
        return lecturesByDay[dayName] ?: emptyList()
    }
    companion object
}

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

@Serializable
data class TimetableUiState(
    val signInRequired: Boolean = true,
    val timetable: WidgetTimetable? = null,
    val lastUpdated: Long = 0L
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
            )
        }
    }
    val lecturesByDay = widgetItems.groupBy { it.day }

    return TimetableUiState(
        signInRequired = false,
        timetable = WidgetTimetable(
            lecturesByDay = lecturesByDay,
            visibleDays = this.visibleDays,
            minMinutes = calculatedMin,
            maxMinutes = calculatedMax
        ),
        lastUpdated = System.currentTimeMillis()
    )
}

fun WidgetTimetable.Companion.mock(): WidgetTimetable {
    val mondayLectures = listOf(
        WidgetLectureEntry(
            title = "소프트웨어 공학",
            classroom = "정보전자공학동 101",
            day = DayType.MON,
            startMinutes = 540,
            durationMinutes = 75,
            bgColor = "#4A90E2",
            textColor = "#FFFFFF"
        ),
        WidgetLectureEntry(
            title = "운영체제",
            classroom = "창의학습관 302",
            day = DayType.MON,
            startMinutes = 650,
            durationMinutes = 105,
            bgColor = "#F5A623",
            textColor = "#FFFFFF"
        )
    )

    return WidgetTimetable(
        lecturesByDay = mapOf(DayType.MON to mondayLectures),
        visibleDays = DayType.weekdays(),
        minMinutes = 540,  // 09:00
        maxMinutes = 1080  // 18:00
    )
}
