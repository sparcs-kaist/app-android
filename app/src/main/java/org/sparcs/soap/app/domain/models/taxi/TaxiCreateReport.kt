package org.sparcs.soap.app.domain.models.taxi

import java.util.Date

data class TaxiCreateReport(
    val reportedID: String,
    val reason: TaxiReport.Reason,
    val etcDetails: String?,
    val time: Date,
    val roomID: String
)
