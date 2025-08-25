package com.example.soap.Domain.Models.Taxi

import java.util.Date

data class TaxiReport(
    val id: String,
    val nickname: String?,
    val reportType: ReportType,
    val reason: ReportReason,
    val etcDetail: String,
    val reportedAt: Date
) {
    enum class ReportType(val value: String) {
        REPORTED("Received"),
        REPORTING("Submitted");

        companion object {
            fun from(value: String): ReportType? =
                entries.find { it.value == value }
        }
    }

    enum class ReportReason(val value: String) {
        NO_SETTLEMENT("no-settlement"),
        NO_SHOW("no-show"),
        ETC("etc-reason");

        companion object {
            fun from(value: String): ReportReason? =
                entries.find { it.value == value }
        }
    }
}
