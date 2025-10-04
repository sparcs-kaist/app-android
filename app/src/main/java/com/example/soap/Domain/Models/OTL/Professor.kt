package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)