package org.sparcs.Domain.Models.OTL

import org.sparcs.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)