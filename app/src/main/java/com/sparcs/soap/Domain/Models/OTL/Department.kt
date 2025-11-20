package com.sparcs.soap.Domain.Models.OTL

import com.sparcs.soap.Domain.Helpers.LocalizedString

data class Department(
    val id: Int,
    val name: LocalizedString,
    val code: String
)