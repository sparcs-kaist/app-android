package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.LectureType

data class SearchCourse(
    val id: Int,
    val name: String,
    val code: String,
    val type: LectureType,
    val summary: String,
) {
    companion object
}