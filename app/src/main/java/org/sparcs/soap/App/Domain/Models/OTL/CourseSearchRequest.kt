package org.sparcs.soap.App.Domain.Models.OTL

data class CourseSearchRequest(
    val keyword: String,
    val limit: Int,
    val offset: Int
)