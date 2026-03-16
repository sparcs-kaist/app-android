package org.sparcs.soap.App.Features.TaxiChat.Components

import org.sparcs.soap.App.Domain.Models.Taxi.ChatBubblePosition
import org.sparcs.soap.App.Domain.Models.Taxi.ChatRenderItem
import org.sparcs.soap.App.Domain.Models.Taxi.SenderInfo
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Shared.Extensions.formattedDate
import java.util.UUID

class ChatRenderItemBuilder(
    private val policy: ChatGroupingPolicy = TaxiGroupingPolicy(),
    private val positionResolver: ChatBubblePositionResolver = ChatBubblePositionResolver(),
    private val presentationPolicy: MessagePresentationPolicy = DefaultMessagePresentationPolicy()
) {
    fun build(chats: List<TaxiChat>, myUserID: String?): List<ChatRenderItem> {
        val mutableChats = chats.sortedWith(compareBy({ it.time }, { it.id })).toMutableList()

        if (mutableChats.none { it.type == TaxiChat.ChatType.SHARE }) {
            val firstInIndex = mutableChats.indexOfFirst { it.type == TaxiChat.ChatType.ENTRANCE }
            if (firstInIndex != -1) {
                val firstIn = mutableChats[firstInIndex]
                val shareMessage = TaxiChat(
                    id = UUID.nameUUIDFromBytes("INITIAL_SHARE_${firstIn.roomID}".toByteArray()),
                    roomID = firstIn.roomID,
                    type = TaxiChat.ChatType.SHARE,
                    authorID = null,
                    authorName = null,
                    authorProfileURL = null,
                    authorIsWithdrew = false,
                    content = "SHARE_PROMPT",
                    time = firstIn.time,
                    isValid = true,
                    inOutNames = null
                )
                mutableChats.add(firstInIndex + 1, shareMessage)
            }
        }

        val items = mutableListOf<ChatRenderItem>()
        val cluster = mutableListOf<TaxiChat>()
        var lastDay: String? = null

        fun flushCluster() {
            if (cluster.isEmpty()) return

            cluster.forEachIndexed { idx, chat ->
                val sender = createSenderInfo(chat, myUserID)
                val pos = positionResolver.resolve(idx, cluster.size)
                val meta = presentationPolicy.metadata(
                    kind = chat.type,
                    isMine = sender.isMine,
                    indexInCluster = idx,
                    clusterCount = cluster.size,
                    isStandalone = false
                )

                items.add(
                    ChatRenderItem.Message(
                        chat.id.toString(),
                        chat,
                        chat.type,
                        sender,
                        pos,
                        meta
                    )
                )
            }
            cluster.clear()
        }

        for (chat in mutableChats) {
            val currentDay = chat.time.formattedDate()
            if (lastDay == null || currentDay != lastDay) {
                flushCluster()
                items.add(ChatRenderItem.DaySeparator(chat.time))
                lastDay = currentDay
            }

            val isMergeable = policy.isBubbleEligible(chat) && !policy.isSystemEvent(chat)

            if (!isMergeable) {
                flushCluster()
                val sender = createSenderInfo(chat, myUserID)
                val meta = presentationPolicy.metadata(chat.type, sender.isMine, 0, 1, true)

                if (chat.type == TaxiChat.ChatType.ENTRANCE || chat.type == TaxiChat.ChatType.EXIT) {
                    items.add(ChatRenderItem.SystemEvent(chat.id.toString(), chat))
                } else {
                    items.add(
                        ChatRenderItem.Message(
                            chat.id.toString(),
                            chat,
                            chat.type,
                            sender,
                            ChatBubblePosition.SINGLE,
                            meta
                        )
                    )
                }
            } else {
                val prev = cluster.lastOrNull()
                if (prev != null && policy.canGroup(prev, chat, myUserID)) {
                    cluster.add(chat)
                } else {
                    flushCluster()
                    cluster.add(chat)
                }
            }
        }

        flushCluster()
        return items
    }

    private fun createSenderInfo(chat: TaxiChat, myUserId: String?) = SenderInfo(
        id = chat.authorID,
        name = chat.authorName,
        avatarURL = chat.authorProfileURL,
        isMine = chat.authorID != null && chat.authorID == myUserId,
        isWithdrew = chat.authorIsWithdrew ?: false
    )
}