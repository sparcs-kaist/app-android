package org.sparcs.App.Domain.Models.Taxi

import org.sparcs.R
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

    enum class Reason(val value: String, val text: Int) {
        NO_SETTLEMENT("no-settlement", R.string.didnot_send_the_money),
        NO_SHOW("no-show", R.string.didnot_come_on_time),
        ETC_REASON("etc-reason", R.string.etc);

        companion object {
            fun from(value: String): Reason = entries.firstOrNull { it.value == value }
                ?: ETC_REASON
        }
    }
}
