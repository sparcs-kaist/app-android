package org.sparcs.soap.widgets.buddyTimetableWidget

import kotlinx.serialization.Serializable
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry

@Serializable
data class WidgetTimetableEntry(
    val lecturesByDay: Map<DayType?, List<WidgetLectureEntry>>,
    val visibleDays: List<DayType>,
    val minMinutes: Int,
    val maxMinutes: Int,
    val activitiesByDay: Map<DayType, List<WidgetLectureEntry>> = emptyMap(),
) {
    fun getLectures(dayName: DayType): List<WidgetLectureEntry> {
        return lecturesByDay[dayName] ?: emptyList()
    }

    fun getEntries(day: DayType): List<WidgetLectureEntry> =
        (getLectures(day) + activitiesByDay[day].orEmpty()).sortedBy { it.startMinutes }

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
