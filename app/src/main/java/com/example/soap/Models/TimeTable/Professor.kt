package com.example.soap.Models.TimeTable

import com.example.soap.Utilities.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)