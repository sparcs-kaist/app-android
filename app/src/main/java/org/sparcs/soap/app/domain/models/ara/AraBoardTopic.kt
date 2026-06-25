package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.helpers.LocalizedString

data class AraBoardTopic(
    val id: Int,
    val slug: String,
    val name: LocalizedString
)