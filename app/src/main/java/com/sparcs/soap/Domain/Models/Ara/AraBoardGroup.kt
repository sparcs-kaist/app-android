package com.sparcs.soap.Domain.Models.Ara

import com.sparcs.soap.Domain.Helpers.LocalizedString

data class AraBoardGroup(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)