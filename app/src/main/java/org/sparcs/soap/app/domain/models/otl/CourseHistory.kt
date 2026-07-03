package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType

data class CourseHistory(
    val year: Int,
    val semester: SemesterType,
    val classes: List<CourseHistoryClass>,
    val myLectureID: Int?
){
    companion object
}