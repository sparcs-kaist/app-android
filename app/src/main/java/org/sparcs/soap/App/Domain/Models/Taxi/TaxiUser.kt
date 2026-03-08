package org.sparcs.soap.App.Domain.Models.Taxi

import java.util.Date

data class TaxiUser(
    val id: String,
    val oid: String,
    val name: String,
    val nickname: String,
    val phoneNumber: String?,
    val badge: Boolean?,
    val residence: String?,
    val email: String,
    val withdraw: Boolean,
    val ban: Boolean,
    val agreeOnTermsOfService: Boolean,
    val joinedAt: Date,
    val profileImageURL: String?,
    val account: String
) {
    companion object {}

    fun hasUserPaid(rooms: List<TaxiRoom>): Boolean {
        return rooms.none { room ->
            room.participants.firstOrNull { it.id == this.oid }?.isSettlement == TaxiParticipant.SettlementType.PaymentRequired
        }
    }
}