package com.example.soap.Domain.Models.Ara

import com.example.soap.Domain.Helpers.LocalizedString

data class AraBoardTopic(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)