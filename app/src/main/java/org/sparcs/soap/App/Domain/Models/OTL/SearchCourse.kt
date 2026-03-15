package org.sparcs.soap.App.Domain.Models.OTL

data class SearchCourse(
    val id: Int,
    val name: String,
    val code: String,
    val type: String,
    val summary: String,
) {
    companion object
}