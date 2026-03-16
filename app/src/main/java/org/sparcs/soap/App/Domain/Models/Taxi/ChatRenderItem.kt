package org.sparcs.soap.App.Domain.Models.Taxi

import java.util.Date

sealed class ChatRenderItem {
    abstract val id: String

    data class DaySeparator(
        val date: Date,
        override val id: String = "day-${date.time}"
    ) : ChatRenderItem()

    data class SystemEvent(
        override val id: String,
        val chat: TaxiChat
    ) : ChatRenderItem()

    data class Message(
        override val id: String,
        val chat: TaxiChat,
        val kind: TaxiChat.ChatType,
        val sender: SenderInfo,
        val position: ChatBubblePosition,
        val metadata: MetadataVisibility
    ) : ChatRenderItem()
}