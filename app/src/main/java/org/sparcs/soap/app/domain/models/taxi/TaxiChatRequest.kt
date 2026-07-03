package org.sparcs.soap.app.domain.models.taxi


data class TaxiChatRequest(
    val roomID: String,
    val type: TaxiChat.ChatType,
    val content: String?
){
    companion object
}