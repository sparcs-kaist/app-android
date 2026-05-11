package org.sparcs.soap.App.Domain.Models.Taxi

data class TaxiFavoriteRoute(
    val id: String,
    val from: TaxiLocation,
    val to: TaxiLocation
)
