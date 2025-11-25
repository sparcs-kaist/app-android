package com.sparcs.soap.Domain.Models.OTL

import com.sparcs.soap.Domain.Enums.OTL.DayType
import com.sparcs.soap.Domain.Helpers.LocalizedString

data class ClassTime(
    val buildingCode: String,
    val classroomName: LocalizedString,
    val classroomNameShort: LocalizedString,
    val roomName: String,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)
