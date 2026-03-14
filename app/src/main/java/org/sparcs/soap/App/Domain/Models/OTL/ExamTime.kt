package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType

data class ExamTime(
    val day: DayType,
    val str: String,
    val begin: Int,
    val end: Int,
)