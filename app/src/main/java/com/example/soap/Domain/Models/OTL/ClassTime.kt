package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.LocalizedString

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
