package org.sparcs.soap.app.domain.models.taxi

data class TaxiFavoriteRoute(
    val id: String,
    val from: TaxiLocation,
    val to: TaxiLocation
)
