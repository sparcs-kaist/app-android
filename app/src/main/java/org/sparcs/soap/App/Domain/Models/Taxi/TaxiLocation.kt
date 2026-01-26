package org.sparcs.soap.App.Domain.Models.Taxi

import org.sparcs.soap.App.Domain.Helpers.LocalizedString

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