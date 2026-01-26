package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class Professor(
    val id: Int,
    val name: LocalizedString,
    val reviewTotalWeight: Double
)