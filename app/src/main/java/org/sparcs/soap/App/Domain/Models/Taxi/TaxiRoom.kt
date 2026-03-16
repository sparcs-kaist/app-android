package org.sparcs.soap.App.Domain.Models.Taxi

import org.sparcs.soap.App.Domain.Enums.Taxi.EmojiIdentifier
import java.util.Date

data class TaxiRoom(
    val id: String,
    val title: String,
    val source: TaxiLocation,
    val destination: TaxiLocation,
    val departAt: Date,
    val participants: List<TaxiParticipant>,
    val madeAt: Date,
    val capacity: Int,
    val settlementTotal: Int?,
    val emojiIdentifier: EmojiIdentifier,
    val isDeparted: Boolean,
    val isOver: Boolean?
){
    companion object { }
}