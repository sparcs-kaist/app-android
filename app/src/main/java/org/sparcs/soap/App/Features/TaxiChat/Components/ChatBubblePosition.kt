package org.sparcs.soap.App.Features.TaxiChat.Components

enum class ChatBubblePosition {
    SINGLE,
    TOP,
    MIDDLE,
    BOTTOM
}

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