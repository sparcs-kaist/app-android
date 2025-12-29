package org.sparcs.App.Domain.Models.OTL

data class LectureSearchRequest(
    val semester: Semester,
    val keyword: String,
    val limit: Int,
    val offset: Int
)