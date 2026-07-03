package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.LectureType

data class CourseLecture(
    val id: Int,
    val name: String,
    val code: String,
    val type: LectureType,
    val lectures: List<Lecture>,
    val completed: Boolean
) {
    companion object
}