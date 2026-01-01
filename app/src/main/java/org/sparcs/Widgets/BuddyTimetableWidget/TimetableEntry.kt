package org.sparcs.Widgets.BuddyTimetableWidget

import kotlinx.serialization.Serializable
import org.sparcs.App.Domain.Enums.OTL.DayType
import org.sparcs.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry

@Serializable
data class WidgetTimetableEntry(
    val lecturesByDay: Map<DayType?, List<WidgetLectureEntry>>,
    val visibleDays: List<DayType>,
    val minMinutes: Int,
    val maxMinutes: Int,
) {
    fun getLectures(dayName: DayType): List<WidgetLectureEntry> {
        return lecturesByDay[dayName] ?: emptyList()
    }

    companion object
}

fun WidgetTimetableEntry.Companion.mock(): WidgetTimetableEntry {
    val mondayLectures = listOf(
        WidgetLectureEntry(
            title = "소프트웨어 공학",
            classroom = "정보전자공학동 101",
            day = DayType.MON,
            startMinutes = 540,
            durationMinutes = 75,
            bgColor = "#4A90E2",
            textColor = "#FFFFFF",
            signInRequired = false
        ),
        WidgetLectureEntry(
            title = "운영체제",
            classroom = "창의학습관 302",
            day = DayType.MON,
            startMinutes = 650,
            durationMinutes = 105,
            bgColor = "#F5A623",
            textColor = "#FFFFFF",
            signInRequired = false
        )
    )

    return WidgetTimetableEntry(
        lecturesByDay = mapOf(DayType.MON to mondayLectures),
        visibleDays = DayType.weekdays(),
        minMinutes = 540,  // 09:00
        maxMinutes = 1080  // 18:00
    )
}
