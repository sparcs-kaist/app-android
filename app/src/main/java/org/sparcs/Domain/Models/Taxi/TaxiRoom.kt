package org.sparcs.Domain.Models.Taxi

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
    val isDeparted: Boolean,
    val isOver: Boolean?
){
    companion object { }
}