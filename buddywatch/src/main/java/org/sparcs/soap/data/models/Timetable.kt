package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable
@Serializable
data class Timetable(
    val id: String,
    val lectures: List<Lecture>,
    val activities: List<TimetableActivity> = emptyList(),
    val textColor: String = "#FFFFFF"
) {
    companion object {
        fun mock(
            id: String = "sample_id",
            lectures: List<Lecture> = Lecture.mockList(),
            activities: List<TimetableActivity> = listOf(
                TimetableActivity(1, "스터디", "MON", 1080, 1140, "도서관", "#845EF7")
            )
        ) = Timetable(id, lectures, activities)
    }
}
