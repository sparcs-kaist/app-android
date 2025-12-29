package org.sparcs.App.Domain.Enums.Taxi

import org.sparcs.App.Domain.Models.Taxi.TaxiReport
import org.sparcs.R

enum class TaxiReportType(val value: Int) {
    INCOMING(R.string.received),
    OUTGOING(R.string.submitted)
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)