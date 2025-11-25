package com.sparcs.soap.Domain.Enums.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiReport

enum class TaxiReportType(val value: String) {
    INCOMING("Received"),
    OUTGOING("Submitted")
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)