package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType

data class LectureExam(
    val day: DayType,
    val description: String,
    val begin: Int,
    val end: Int,
)