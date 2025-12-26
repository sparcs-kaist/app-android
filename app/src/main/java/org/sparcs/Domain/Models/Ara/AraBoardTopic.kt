package org.sparcs.Domain.Models.Ara

import org.sparcs.Domain.Helpers.LocalizedString

data class AraBoardTopic(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)