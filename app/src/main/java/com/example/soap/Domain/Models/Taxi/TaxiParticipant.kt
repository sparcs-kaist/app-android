package com.example.soap.Domain.Models.Taxi

import java.net.URL
import java.util.Date

data class TaxiParticipant(
    val id: String,
    val name: String,
    val nickname: String,
    val profileImageURL: URL?,
    val withdraw: Boolean,
    val isSettlement: SettlementType?,
    val readAt: Date
) {
    enum class SettlementType(val rawValue: String) {
        NotDeparted("not-departed"),
        RequestedSettlement("paid"),
        PaymentRequired("send-required"),
        PaymentSent("sent");

        companion object {
            fun from(raw: String): SettlementType? =
                entries.firstOrNull { it.rawValue == raw }
        }
    }
}