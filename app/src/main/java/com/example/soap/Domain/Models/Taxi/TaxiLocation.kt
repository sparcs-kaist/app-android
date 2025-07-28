package com.example.soap.Domain.Models.Taxi

import com.example.soap.Domain.Helpers.LocalizedString

data class TaxiLocation(
    val id: String,
    val title: LocalizedString,
    val priority: Double?,
    val latitude: Double,
    val longitude: Double
) {
    companion object{}
}