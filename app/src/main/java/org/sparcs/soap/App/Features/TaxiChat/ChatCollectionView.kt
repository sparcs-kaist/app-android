package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Taxi.ChatBubblePosition
import org.sparcs.soap.App.Domain.Models.Taxi.ChatRenderItem
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatBubblePositionResolver
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatRenderItemBuilder
import org.sparcs.soap.App.Features.TaxiChat.Components.DefaultMessagePresentationPolicy
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiGroupingPolicy
import org.sparcs.soap.App.Shared.Extensions.toLocalDate
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun ChatCollectionView(
    items: List<ChatRenderItem>,
    room: TaxiRoom,
    user: TaxiUser?,
    onCommitPayment: () -> Unit,
    onImageClick: (String) -> Unit,
    listState: LazyListState,
    scrollToBottomTrigger: Int,
    modifier: Modifier = Modifier
) {
    val badgeByAuthorID = remember(room.participants) {
        room.participants.associate { it.id to it.badge }
    }

    val isCommitSettlementAvailable = remember(room, user?.oid) {
        val departed = room.isDeparted
        val myParticipantInfo = user?.let { currentUser ->
            room.participants.find { it.id == currentUser.oid }
        }
        val iHaveNotSettled = myParticipantInfo?.isSettlement?.let {
            it != TaxiParticipant.SettlementType.PaymentSent
        } ?: false
        departed && iHaveNotSettled
    }

    LaunchedEffect(scrollToBottomTrigger) {
        if (items.isNotEmpty()) {
            listState.animateScrollToItem(items.size - 1)
        }
    }

    LaunchedEffect(items.isNotEmpty()) {
        if (items.isNotEmpty()) {
            listState.scrollToItem(items.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            ChatItem(
                item = item,
                room = room,
                user = user,
                onCommitPayment = { onCommitPayment() },
                isCommitSettlementAvailable = isCommitSettlementAvailable,
                onImageClick = onImageClick,
                hasBadge = { authorID -> authorID?.let { badgeByAuthorID[it] } ?: false }
            )
        }
    }
}

@Composable
private fun ChatItem(
    item: ChatRenderItem,
    room: TaxiRoom,
    user: TaxiUser?,
    isCommitSettlementAvailable: Boolean,
    onCommitPayment: () -> Unit,
    onImageClick: (String) -> Unit,
    hasBadge: (String?) -> Boolean
) {
    when (item) {
        is ChatRenderItem.DaySeparator -> {
            ChatDaySeparator(
                date = item.date.toLocalDate()
            )
        }

        is ChatRenderItem.SystemEvent -> {
            ChatGeneralMessage(
                authorName = item.chat.authorName,
                type = item.chat.type
            )
        }

        is ChatRenderItem.Message -> {
            MessageView(
                chat = item.chat,
                kind = item.chat.type,
                sender = item.sender,
                position = item.position,
                readCount = calculateReadCount(item.chat, room, user),
                metadata = item.metadata,
                hasBadge = hasBadge(item.sender.id),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(
                        top = if (item.position == ChatBubblePosition.MIDDLE ||
                            item.position == ChatBubblePosition.BOTTOM
                        ) 4.dp else 8.dp
                    )
            ) {
                when (item.chat.type) {
                    TaxiChat.ChatType.TEXT -> ChatBubble(item.chat, item.position, item.sender.isMine)
                    TaxiChat.ChatType.S3IMG -> ChatImageBubble(id = item.chat.content, onClick = onImageClick)
                    TaxiChat.ChatType.DEPARTURE -> ChatDepartureBubble(room = room)
                    TaxiChat.ChatType.ARRIVAL -> ChatArrivalBubble()
                    TaxiChat.ChatType.SETTLEMENT -> ChatSettlementBubble()
                    TaxiChat.ChatType.PAYMENT -> ChatPaymentBubble()
                    TaxiChat.ChatType.ACCOUNT -> ChatAccountBubble(
                        content = item.chat.content,
                        isCommitPaymentAvailable = isCommitSettlementAvailable
                    ) {
                        onCommitPayment()
                    }
                    TaxiChat.ChatType.SHARE -> ChatShareBubble(room = room)
                    else -> Text(stringResource(R.string.not_supported))
                }
            }
        }
    }
}

private fun calculateReadCount(chat: TaxiChat, room: TaxiRoom, user: TaxiUser?): Int {
    val otherParticipants = room.participants.filter { it.id != user?.oid }
    return otherParticipants.count { it.readAt <= chat.time }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChatCollectionViewPreview() {
    val mockChats = TaxiChat.mockList()

    val builder = ChatRenderItemBuilder(
        policy = TaxiGroupingPolicy(),
        positionResolver = ChatBubblePositionResolver(),
        presentationPolicy = DefaultMessagePresentationPolicy()
    )
    val items = builder.build(chats = mockChats, myUserID = "user2")

    val listState = rememberLazyListState()

    Theme {
        Box(modifier = Modifier.fillMaxSize()) {
            ChatCollectionView(
                items = items,
                room = TaxiRoom.mock(),
                user = TaxiUser.mock(),
                onCommitPayment = {},
                onImageClick = {},
                listState = listState,
                scrollToBottomTrigger = 0
            )
        }
    }
}