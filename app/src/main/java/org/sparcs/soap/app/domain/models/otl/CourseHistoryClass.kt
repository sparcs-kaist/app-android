package org.sparcs.soap.app.domain.models.otl

data class CourseHistoryClass(
    val lectureID: Int,
    val subtitle: String,
    val section: String,
    val professors: List<Professor>
){
    companion object
}