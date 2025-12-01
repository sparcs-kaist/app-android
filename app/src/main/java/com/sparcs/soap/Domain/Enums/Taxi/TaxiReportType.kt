package com.sparcs.soap.Domain.Enums.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiReport
import com.sparcs.soap.R

enum class TaxiReportType(val value: Int) {
    INCOMING(R.string.received),
    OUTGOING(R.string.submitted)
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)