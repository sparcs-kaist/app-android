package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class Department(
    val id: Int,
    val name: LocalizedString,
    val code: String
)