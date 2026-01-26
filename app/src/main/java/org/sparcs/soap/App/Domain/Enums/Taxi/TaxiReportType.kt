package org.sparcs.soap.App.Domain.Enums.Taxi

import org.sparcs.soap.App.Domain.Models.Taxi.TaxiReport
import org.sparcs.soap.R

enum class TaxiReportType(val value: Int) {
    INCOMING(R.string.received),
    OUTGOING(R.string.submitted)
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)