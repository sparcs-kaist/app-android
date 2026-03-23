package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.LectureType

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