package org.sparcs.App.Domain.Models.OTL

import org.sparcs.App.Domain.Enums.OTL.DayType
import org.sparcs.App.Domain.Helpers.LocalizedString

data class ExamTime(
    val description: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)