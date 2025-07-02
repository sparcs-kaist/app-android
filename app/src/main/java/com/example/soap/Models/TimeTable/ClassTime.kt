package com.example.soap.Models.TimeTable

import com.example.soap.Models.Types.DayType
import com.example.soap.Utilities.Helpers.LocalizedString

data class ClassTime(
    val classroomName: LocalizedString,
    val classroomNameShort: LocalizedString,
    val roomName: String,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)
