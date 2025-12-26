package org.sparcs.Domain.Models.Taxi

import java.util.Date

data class TaxiCreateRoom(
    val title: String,
    val source: TaxiLocation,
    val destination: TaxiLocation,
    val departureTime: Date,
    val capacity: Int
)