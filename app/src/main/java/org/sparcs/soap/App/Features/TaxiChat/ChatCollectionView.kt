package org.sparcs.soap.App.Features.TaxiChat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
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
    modifier: Modifier = Modifier,
) {
    val badgeByAuthorID = remember(room.participants) {
        room.participants.associate { it.id to it.badge }
    }

    val isCommitPaymentAvailable = remember(room, user?.oid) {
        val departed = room.isDeparted
        val myParticipantInfo = user?.let { currentUser ->
            room.participants.find { it.id == currentUser.oid }
        }
        val paymentRequired = myParticipantInfo?.isSettlement?.let {
            it == TaxiParticipant.SettlementType.PaymentRequired
        } ?: false
        departed && paymentRequired
    }

    val isPayer = remember(room, user?.oid) {
        val myParticipantInfo = user?.let { currentUser ->
            room.participants.find { it.id == currentUser.oid }
        }
        myParticipantInfo?.isSettlement == TaxiParticipant.SettlementType.RequestedSettlement
    }

    val latestSettlementMeta = remember(items) {
        items.filterIsInstance<ChatRenderItem.Message>()
            .map { it.chat }
            .lastOrNull { it.type == TaxiChat.ChatType.SETTLEMENT }
            ?.settlementMeta
    }

    LaunchedEffect(scrollToBottomTrigger) {
        if (items.isNotEmpty() && scrollToBottomTrigger > 0) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(items.isNotEmpty()) {
        if (items.isNotEmpty() && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            listState.scrollToItem(0)
        }
    }

    val reversedItems = remember(items) { items.reversed() }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(top = 8.dp),
        reverseLayout = true
    ) {
        items(
            items = reversedItems,
            key = { it.id }
        ) { item ->
            ChatItem(
                item = item,
                room = room,
                user = user,
                isCommitPaymentAvailable = isCommitPaymentAvailable,
                isPayer = isPayer,
                settlementMeta = latestSettlementMeta,
                onCommitPayment = { onCommitPayment() },
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
    isCommitPaymentAvailable: Boolean,
    isPayer: Boolean,
    settlementMeta: TaxiChat.SettlementMeta?,
    onCommitPayment: () -> Unit,
    onImageClick: (String) -> Unit,
    hasBadge: (String?) -> Boolean,
) {
    when (item) {
        is ChatRenderItem.DaySeparator -> {
            _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatDaySeparator(
                date = item.date.toLocalDate()
            )
        }

        is ChatRenderItem.SystemEvent -> {
            _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatGeneralMessage(
                authorName = item.chat.authorName,
                type = item.chat.type
            )
        }

        is ChatRenderItem.Message -> {
            _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.MessageView(
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
                    TaxiChat.ChatType.TEXT -> if (user == null) {
                        _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatBubbleSkeleton()
                    } else {
                        _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatBubble(
                            item.chat,
                            item.position,
                            item.sender.isMine
                        )
                    }

                    TaxiChat.ChatType.S3IMG -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatImageBubble(
                        id = item.chat.content,
                        onClick = onImageClick
                    )

                    TaxiChat.ChatType.DEPARTURE -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatDepartureBubble(
                        room = room,
                        chatTime = item.chat.time
                    )

                    TaxiChat.ChatType.ARRIVAL -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatArrivalBubble()
                    TaxiChat.ChatType.SETTLEMENT -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatSettlementBubble()
                    TaxiChat.ChatType.PAYMENT -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatPaymentBubble()
                    TaxiChat.ChatType.ACCOUNT -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatAccountBubble(
                        content = item.chat.content,
                        totalAmount = settlementMeta?.total,
                        perPersonAmount = settlementMeta?.perPerson,
                        isCommitPaymentAvailable = isCommitPaymentAvailable,
                        isPayer = isPayer
                    ) {
                        onCommitPayment()
                    }

                    TaxiChat.ChatType.SHARE -> _root_ide_package_.org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatShareBubble(
                        room = room
                    )

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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
@Composable
private fun ChatCollectionViewSkeletonPreview() {
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
                user = null,
                onCommitPayment = {},
                onImageClick = {},
                listState = listState,
                scrollToBottomTrigger = 0
            )
        }
    }
}