package org.sparcs.soap.data.models

import kotlinx.serialization.Serializable
@Serializable
data class Timetable(
    val id: String,
    val lectures: List<Lecture>
) {
    companion object {
        fun mock(
            id: String = "sample_id",
            lectures: List<Lecture> = Lecture.mockList()
        ) = Timetable(id, lectures)
    }
}