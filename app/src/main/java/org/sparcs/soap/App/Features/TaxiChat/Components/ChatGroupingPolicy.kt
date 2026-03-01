package org.sparcs.soap.App.Features.TaxiChat.Components

import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat

interface ChatGroupingPolicy {
    fun isBubbleEligible(chat: TaxiChat): Boolean
    fun isSystemEvent(chat: TaxiChat): Boolean
    fun canGroup(lhs: TaxiChat, rhs: TaxiChat, myUserID: String?): Boolean
}

class TaxiGroupingPolicy : ChatGroupingPolicy {
    private val maxGapMillis: Long = 60 * 1000L

    override fun isBubbleEligible(chat: TaxiChat): Boolean {
        return when (chat.type) {
            TaxiChat.ChatType.TEXT, TaxiChat.ChatType.S3IMG -> true
            else -> false
        }
    }

    override fun isSystemEvent(chat: TaxiChat): Boolean {
        return when (chat.type) {
            TaxiChat.ChatType.ENTRANCE,
            TaxiChat.ChatType.EXIT,
            TaxiChat.ChatType.DEPARTURE,
            TaxiChat.ChatType.ARRIVAL,
            TaxiChat.ChatType.SETTLEMENT,
            TaxiChat.ChatType.SHARE -> true
            else -> false
        }
    }

    override fun canGroup(lhs: TaxiChat, rhs: TaxiChat, myUserID: String?): Boolean {
        if (!isBubbleEligible(lhs) || !isBubbleEligible(rhs)) return false

        if (lhs.authorID != rhs.authorID) return false

        val lhsIsMine = lhs.authorID == myUserID
        val rhsIsMine = rhs.authorID == myUserID
        if (lhsIsMine != rhsIsMine) return false

        val gap = rhs.time.time - lhs.time.time
        return gap in 0..maxGapMillis
    }
}