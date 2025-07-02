package com.example.soap.Models.TimeTable

import com.example.soap.Models.Types.DayType
import com.example.soap.Utilities.Helpers.LocalizedString

data class ExamTime(
    val str: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)