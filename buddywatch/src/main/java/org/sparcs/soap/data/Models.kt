package org.sparcs.soap.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Timetable(
    val id: String,
    val lectures: List<Lecture>
)

@Serializable
data class Lecture(
    val id: Int,
    val name: String,
    val classes: List<LectureClass>,
    val color: String? = null // Hex color string
)

@Serializable
data class LectureClass(
    val day: String, // MON, TUE, etc.
    val begin: Int,
    val end: Int,
    @SerialName("location") val location: String
)