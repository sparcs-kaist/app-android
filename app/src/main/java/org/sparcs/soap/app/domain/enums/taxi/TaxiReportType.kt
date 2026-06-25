package org.sparcs.soap.app.domain.enums.taxi

import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiReport

enum class TaxiReportType(val value: Int) {
    INCOMING(R.string.received),
    OUTGOING(R.string.submitted)
}

data class TaxiReports(
    val incoming: List<TaxiReport>,
    val outgoing: List<TaxiReport>
)