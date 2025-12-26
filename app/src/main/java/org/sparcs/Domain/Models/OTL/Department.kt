package org.sparcs.Domain.Models.OTL

import org.sparcs.Domain.Helpers.LocalizedString

data class Department(
    val id: Int,
    val name: LocalizedString,
    val code: String
)