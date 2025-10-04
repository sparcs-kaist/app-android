package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Helpers.LocalizedString

data class Course(
    val id: Int,
    val code: String,
    val department: Department,
    val type: LocalizedString,
    val title: LocalizedString,
    val summary: String,
    val reviewTotalWeight: Double,
    val grade: Double,
    val load: Double,
    val speech: Double,
    val credit: Int,
    val creditAu: Int,
    val numClasses: Int,
    val numLabs: Int
)