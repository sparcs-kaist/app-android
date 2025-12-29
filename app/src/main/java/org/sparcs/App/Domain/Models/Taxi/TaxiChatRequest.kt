package org.sparcs.App.Domain.Models.Taxi


data class TaxiChatRequest(
    val roomID: String,
    val type: TaxiChat.ChatType,
    val content: String?
){
    companion object{}
}