package com.sparcs.soap.Domain.Models.OTL

import com.sparcs.soap.Domain.Enums.DayType
import com.sparcs.soap.Domain.Helpers.LocalizedString

data class ExamTime(
    val description: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)