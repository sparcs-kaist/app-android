package org.sparcs.soap.app.domain.models.otl

data class LectureSearchRequest(
    val semester: Semester,
    val keyword: String,
    val limit: Int,
    val offset: Int
)