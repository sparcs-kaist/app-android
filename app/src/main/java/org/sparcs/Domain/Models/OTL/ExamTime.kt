package org.sparcs.Domain.Models.OTL

import org.sparcs.Domain.Enums.OTL.DayType
import org.sparcs.Domain.Helpers.LocalizedString

data class ExamTime(
    val description: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)