package org.sparcs.soap.app.features.taxiChat.components

import org.sparcs.soap.app.domain.models.taxi.ChatBubblePosition


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