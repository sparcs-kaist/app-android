package org.sparcs.soap.App.Domain.Models.OTL

data class Course(
    val id: Int,
    val name: String,
    val code: String,
    val type: String,
    val department: Department,
    val summary: String,
    val classDuration: Int,
    val expDuration: Int,
    val credit: Int,
    val creditAu: Int
) {
    companion object
}