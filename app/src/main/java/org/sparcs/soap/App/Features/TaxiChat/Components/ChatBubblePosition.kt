package org.sparcs.soap.App.Features.TaxiChat.Components

import org.sparcs.soap.App.Domain.Models.Taxi.ChatBubblePosition


class ChatBubblePositionResolver {
    fun resolve(index: Int, count: Int): ChatBubblePosition {
        if (count <= 1) return ChatBubblePosition.SINGLE

        return when (index) {
            0 -> ChatBubblePosition.TOP
            count - 1 -> ChatBubblePosition.BOTTOM
            else -> ChatBubblePosition.MIDDLE
        }
    }
}