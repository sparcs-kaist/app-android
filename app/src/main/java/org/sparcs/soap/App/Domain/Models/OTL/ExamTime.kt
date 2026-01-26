package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class ExamTime(
    val description: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)