package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.LectureType

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