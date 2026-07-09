package org.sparcs.soap.app.domain.models.otl

data class LectureSearchRequest(
    val semester: Semester,
    val keyword: String,
    val limit: Int,
    val offset: Int,
    val type: List<String>? = null,
    val department: List<String>? = null,
    val level: List<String>? = null,
    val term: String? = null
)