package org.sparcs.soap.App.Domain.Models.Ara

import org.sparcs.soap.App.Domain.Helpers.LocalizedString

data class AraBoardGroup(
    val id: Int,
    val slug: String,
    val name: LocalizedString
) {
        companion object {
            val Empty = AraBoardGroup(
                id = 999,
                slug = "extra",
                name = LocalizedString(
                    mapOf(
                        "ko" to "기타",
                        "en" to "Extra"
                    )
                )
            )
        }
}