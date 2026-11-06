package org.sparcs.soap.taxiChatTests

import org.junit.Assert.assertEquals
import org.junit.Test
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.models.taxi.ChatBubblePosition
import org.sparcs.soap.app.domain.models.taxi.ChatRenderItem
import org.sparcs.soap.app.domain.models.taxi.MetadataVisibility
import org.sparcs.soap.app.domain.models.taxi.SenderInfo
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.features.taxiChat.ChatGallery
import java.util.Date

class ChatGalleryTest {
    private fun message(type: TaxiChat.ChatType, content: String): ChatRenderItem {
        val chat = TaxiChat(
            roomID = "room", type = type, authorID = "u", authorName = "U", authorProfileURL = null,
            authorIsWithdrew = false, content = content, time = Date(), isValid = true, inOutNames = null,
        )
        return ChatRenderItem.Message(
            id = chat.id.toString(), chat = chat, kind = type,
            sender = SenderInfo("u", "U", null, isMine = false, isWithdrew = false),
            position = ChatBubblePosition.SINGLE,
            metadata = MetadataVisibility(showName = true, showAvatar = true, showTime = true),
        )
    }

    private val items = listOf(
        ChatRenderItem.DaySeparator(Date(0)),
        message(TaxiChat.ChatType.S3IMG, "a"),
        message(TaxiChat.ChatType.TEXT, "hello"),
        message(TaxiChat.ChatType.S3IMG, "b"),
        message(TaxiChat.ChatType.S3IMG, "c"),
    )

    @Test
    fun opensEveryPhotoAtTheTappedOne() {
        val gallery = ChatGallery.of(items, selectedId = "b")
        assertEquals(listOf("a", "b", "c").map { Constants.taxiChatImageURL + it }, gallery.urls)
        assertEquals(1, gallery.startIndex)
    }

    @Test
    fun opensAPhotoMissingFromTheListAlone() {
        val gallery = ChatGallery.of(items, selectedId = "z")
        assertEquals(listOf(Constants.taxiChatImageURL + "z"), gallery.urls)
        assertEquals(0, gallery.startIndex)
    }
}
