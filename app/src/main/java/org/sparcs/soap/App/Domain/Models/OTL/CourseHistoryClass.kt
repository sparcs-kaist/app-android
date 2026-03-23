package org.sparcs.soap.App.Domain.Models.OTL

data class CourseHistoryClass(
    val lectureID: Int,
    val subtitle: String,
    val section: String,
    val professors: List<Professor>
){
    companion object
}