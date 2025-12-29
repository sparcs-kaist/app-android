package org.sparcs.App.Domain.Models.Ara

import org.sparcs.App.Domain.Helpers.LocalizedString

data class AraBoardTopic(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)