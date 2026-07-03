package org.sparcs.soap.app.domain.models.ara

import org.sparcs.soap.app.domain.helpers.LocalizedString

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