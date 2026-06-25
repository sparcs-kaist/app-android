package org.sparcs.soap.app.domain.models.taxi

import org.sparcs.soap.app.domain.helpers.LocalizedString

data class TaxiLocation(
    val id: String,
    val title: LocalizedString,
    val priority: Double?,
    val latitude: Double,
    val longitude: Double
) {
    companion object
    fun titleContains(text: String): Boolean {
        return title.contains(text)
    }
}