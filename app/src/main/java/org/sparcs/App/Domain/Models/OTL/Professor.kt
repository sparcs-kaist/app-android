package org.sparcs.App.Domain.Models.OTL

import org.sparcs.App.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)