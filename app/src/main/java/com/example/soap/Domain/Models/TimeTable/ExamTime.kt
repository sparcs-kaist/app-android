package com.example.soap.Domain.Models.TimeTable

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.LocalizedString

data class ExamTime(
    val str: LocalizedString,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)