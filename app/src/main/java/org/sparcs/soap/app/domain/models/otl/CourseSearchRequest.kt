package org.sparcs.soap.app.domain.models.otl

data class CourseSearchRequest(
    val keyword: String,
    val limit: Int,
    val offset: Int
)