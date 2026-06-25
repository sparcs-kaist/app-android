package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.LectureType

data class CourseSummary(
    val id: Int,
    val code: String,
    val name: String,
    val summary: String,
    val department: Department,
    val professors: List<Professor>,
    val type: LectureType,
    val completed: Boolean,
    val open: Boolean
) {
    companion object
}