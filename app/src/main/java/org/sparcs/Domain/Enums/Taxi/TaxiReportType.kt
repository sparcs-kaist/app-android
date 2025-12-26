package org.sparcs.Domain.Enums.Taxi

import org.sparcs.R
import org.sparcs.Domain.Models.Taxi.TaxiReport

enum class TaxiReportType(val value: Int) {
    INCOMING(R.string.received),
    OUTGOING(R.string.submitted)
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)