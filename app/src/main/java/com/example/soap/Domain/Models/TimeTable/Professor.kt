package com.example.soap.Domain.Models.TimeTable

import com.example.soap.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)