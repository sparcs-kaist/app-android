package com.sparcs.soap.Domain.Models.OTL

import com.sparcs.soap.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)