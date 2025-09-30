package com.example.soap.Domain.Models.Taxi

import java.net.URL
import java.util.Date

data class TaxiReportedUser(
    val id: String,
    val oid: String,
    val nickname: String,
    val profileImageUrl: URL?,
    val withdraw: Boolean
)

data class TaxiReport(
    val id: String,
    val creatorId: String,
    val reportedUser: TaxiReportedUser,
    val reason: Reason,
    val etcDetails: String,
    val time: Date,
    val roomId: String?
) {
    companion object{}

    enum class Reason(val value: String, val text: String) {
        NO_SETTLEMENT("no-settlement", "Didn't send money!"),
        NO_SHOW("no-show", "Didn't come on time!"),
        ETC_REASON("etc-reason", "Etc");

        companion object {
            fun from(value: String): Reason = entries.firstOrNull { it.value == value }
                ?: ETC_REASON
        }
    }
}
