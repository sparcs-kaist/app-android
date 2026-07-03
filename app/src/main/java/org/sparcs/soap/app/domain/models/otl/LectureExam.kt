package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.DayType

data class LectureExam(
    val day: DayType,
    val description: String,
    val begin: Int,
    val end: Int,
)