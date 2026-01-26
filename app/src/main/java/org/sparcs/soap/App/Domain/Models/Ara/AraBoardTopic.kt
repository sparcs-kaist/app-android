package org.sparcs.soap.App.Domain.Models.Ara

import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class AraBoardTopic(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)