package org.sparcs.soap.app.domain.models.taxi

import org.sparcs.soap.app.domain.helpers.LocalizedString

data class TaxiLocation(
    val id: String,
    val title: LocalizedString,
    val priority: Double?,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        private val popularIDs = listOf("636c70c308eab94199a3cbff", "636c70c408eab94199a3cc05")

        fun popularFirst(locations: List<TaxiLocation>): Pair<List<TaxiLocation>, List<TaxiLocation>> {
            val popular = popularIDs.mapNotNull { id -> locations.find { it.id == id } }
            return popular to locations.filterNot { it.id in popularIDs }
        }
    }

    fun titleContains(text: String): Boolean {
        return title.contains(text)
    }
}