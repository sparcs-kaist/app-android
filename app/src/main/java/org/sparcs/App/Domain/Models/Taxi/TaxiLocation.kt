package org.sparcs.App.Domain.Models.Taxi

import org.sparcs.App.Domain.Helpers.LocalizedString

data class TaxiLocation(
    val id: String,
    val title: LocalizedString,
    val priority: Double?,
    val latitude: Double,
    val longitude: Double
) {
    companion object{}
    fun titleContains(text: String): Boolean {
        return title.contains(text)
    }
}