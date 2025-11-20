package com.sparcs.soap.Domain.Models.Taxi

import java.net.URL
import java.util.Date
import java.util.UUID

data class TaxiChatGroup(
    val id: String,
    val chats: List<TaxiChat>,
    val lastChatID: UUID?,
    val authorID: String?,
    val authorName: String?,
    val authorProfileURL: URL?,
    val authorIsWithdrew: Boolean?,
    val time: Date,       // to display time
    val isMe: Boolean,    // if sender of chats is me
    val isGeneral: Boolean // if chat type is entrance/exit, no user wrapper shown
){
    companion object {}
}
