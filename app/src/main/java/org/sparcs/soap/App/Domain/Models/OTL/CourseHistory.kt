package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType

data class CourseHistory(
    val year: Int,
    val semester: SemesterType,
    val classes: List<CourseHistoryClass>,
    val myLectureID: Int?
){
    companion object
}