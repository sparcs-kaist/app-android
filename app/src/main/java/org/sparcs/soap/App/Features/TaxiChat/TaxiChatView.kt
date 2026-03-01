package org.sparcs.soap.App.Features.TaxiChat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChatGroup
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.FullscreenImage.FullscreenImageView
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatAccountBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatArrivalBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatDaySeperator
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatDepartureBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatGeneralMessage
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatImageBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatPaymentBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatSettlementBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatShareBubble
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.TaxiChatBubble
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatReadReceipt
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiChatInputBar
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiChatUserWrapper
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiChatViewNavigationBar
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiDeepLinkHelper
import org.sparcs.soap.App.Shared.Extensions.openUri
import org.sparcs.soap.App.Shared.Extensions.toLocalDate
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.R

@Composable
fun TaxiChatView(
    viewModel: TaxiChatViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val groupedChats by viewModel.groupedChats.collectAsState(initial = emptyList())
    val taxiUser by viewModel.taxiUser.collectAsState()

    var text by remember { mutableStateOf("") }
    var tappedImageID by remember { mutableStateOf<String?>(null) }
    var showCallTaxiAlert by remember { mutableStateOf(false) }
    var showPaymentAlert by remember { mutableStateOf(false) }
    var scrollToBottomTrigger by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val room = viewModel.room.collectAsState().value

    LaunchedEffect(room.id) {
        try {
            viewModel.setup()
        } catch (e: Exception) {
            viewModel.alertState = AlertState(
                titleResId = R.string.error_fetch_chat,
                message = e.localizedMessage ?: "Unknown error"
            )
            viewModel.isAlertPresented = true
        }
    }

    Scaffold(
        topBar = {
            TaxiChatViewNavigationBar(
                room = room,
                onDismiss = { navController.popBackStack() },
                onClickCallTaxi = { showCallTaxiAlert = true },
                onClickLeave = {
                    coroutineScope.launch {
                        viewModel.leaveRoom()
                        navController.popBackStack()
                    }
                },
                onReport = {
                    val json = Uri.encode(Gson().toJson(room))
                    navController.navigate(Channel.TaxiReportView.name + "?room_json=$json")
                },
                isEnabled = viewModel.isLeaveRoomAvailable
            )
        },

        bottomBar = {
            TaxiChatInputBar(
                text = text,
                onTextChange = { text = it },
                taxiUser = taxiUser,
                isUploading = viewModel.isUploading.collectAsState().value,
                isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable,
                isCommitSettlementAvailable = viewModel.isCommitSettlementAvailable,
                onSendText = { message ->
                    coroutineScope.launch {
                        viewModel.sendChat(message, TaxiChat.ChatType.TEXT)
                        scrollToBottomTrigger += 1
                    }
                },
                onSendImage = { bitmap ->
                    coroutineScope.launch {
                        viewModel.sendImage(bitmap)
                        scrollToBottomTrigger += 1
                    }
                },
                onCommitPayment = { showPaymentAlert = true },
                onCommitSettlement = {
                    coroutineScope.launch {
                        viewModel.commitSettlement()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            when (state) {
                is TaxiChatViewModel.ViewState.Loading -> {
                    val mock = TaxiChat.mockList().take(6)

                    ChatListView(
                        groupedChats = mock,
                        isInteractive = false,
                        viewModel = viewModel,
                        onImageClick = {},
                        onCommitPayment = {},
                        modifier = Modifier
                            .alpha(0.5f)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                    }
                                }
                            }
                    )
                }

                is TaxiChatViewModel.ViewState.Error -> ErrorView(
                    icon = Icons.Default.Warning,
                    message = (state as TaxiChatViewModel.ViewState.Error).message,
                    onRetry = { coroutineScope.launch { viewModel.fetchInitialChats() } }
                )

                is TaxiChatViewModel.ViewState.Loaded -> {
                    ChatListView(
                        groupedChats = groupedChats,
                        isInteractive = true,
                        viewModel = viewModel,
                        listState = listState,
                        onImageClick = { tappedImageID = it },
                        onCommitPayment = { showPaymentAlert = true },
                    )
                }
            }
        }
    }

    if (tappedImageID != null) {
        FullscreenImageView(
            id = tappedImageID,
            onDismiss = { tappedImageID = null }
        )
    }

    if (showCallTaxiAlert) {
        AlertDialog(
            onDismissRequest = { showCallTaxiAlert = false },
            title = { Text(stringResource(R.string.call_taxi)) },
            text = {
                Text(stringResource(R.string.taxi_launch_info))
            },
            dismissButton = {
                TextButton(onClick = {
                    showCallTaxiAlert = false
                }) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        val uri = TaxiDeepLinkHelper.getKakaoTUri(room.source, room.destination)
                        context.openUri(uri, "com.kakao.taxi")
                    }) { Text(stringResource(R.string.open_kakao_t)) }
                    TextButton(onClick = {
                        val uberUri = TaxiDeepLinkHelper.getUberUri(room.source ,room.destination)
                        context.openUri(uberUri, "com.ubercab")
                    }) { Text(stringResource(R.string.open_uber)) }
                }
            }
        )
    }

    if (showPaymentAlert) {
        AlertDialog(
            onDismissRequest = { showPaymentAlert = false },
            dismissButton = {
                Button(onClick = { showPaymentAlert = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.send_payment)) },
            text = {
                Text(stringResource(R.string.payment_send_instructions))
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = {
                        val uri = TaxiDeepLinkHelper.getKakaoPayUri(context, viewModel.account)
                        context.openUri(uri, "com.kakao.talk")
                    }) {
                        Text(stringResource(R.string.open_kakao_pay))
                    }
                    TextButton(onClick = {
                        val uri = TaxiDeepLinkHelper.getTossUri(viewModel.account)
                        context.openUri(uri, "viva.republica.toss")
                    }) {
                        Text(stringResource(R.string.open_toss))
                    }
                    TextButton(onClick = { coroutineScope.launch { viewModel.commitPayment() } }) {
                        Text(stringResource(R.string.already_sent))
                    }
                }
            }
        )
    }

    if (viewModel.isAlertPresented) {
        AlertDialog(
            onDismissRequest = { viewModel.isAlertPresented = false },
            confirmButton = {
                TextButton(onClick = { viewModel.isAlertPresented = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = {
                viewModel.alertState?.titleResId?.let { Text(stringResource(it)) }
            },
            text = {
                viewModel.alertState?.let { state ->
                    Text(
                        state.message ?: stringResource(
                            state.messageResId ?: R.string.unexpected_error
                        )
                    )
                }
            }
        )
    }
}

// isInteractive means data is actual loaded data. False means it is a mock and needs to be redacted.
@Composable
private fun ChatListView(
    groupedChats: List<TaxiChatGroup>,
    isInteractive: Boolean,
    viewModel: TaxiChatViewModelProtocol,
    onImageClick: (String) -> Unit,
    onCommitPayment: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    if (isInteractive) {
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { index ->
                    if (index == 0 && groupedChats.isNotEmpty()) {
                        viewModel.loadMoreChats()
                    }
                }
        }
    }

    val taxiUser by viewModel.taxiUser.collectAsState(initial = null)
    val room by viewModel.room.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .then(if (!isInteractive) Modifier.pointerInput(Unit) {} else Modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
    ) {
        if (groupedChats.isNotEmpty()) {
            item {
                ChatDaySeperator(date = groupedChats.first().time.toLocalDate())
            }
        }

        itemsIndexed(groupedChats, key = { _, group -> group.id }) { index, group ->
            if (index > 0) {
                val prevDate = groupedChats[index - 1].time.toLocalDate()
                val currentDate = group.time.toLocalDate()
                if (currentDate != prevDate) {
                    ChatDaySeperator(date = currentDate)
                }
            }

            TaxiChatUserWrapper(
                authorID = group.authorID,
                authorName = group.authorName,
                authorProfileImageURL = group.authorProfileURL,
                date = group.time,
                isMe = if (isInteractive) group.isMe else false,
                isGeneral = group.isGeneral,
                isWithdrawn = group.authorIsWithdrew ?: false,
                badge = if (isInteractive) viewModel.hasBadge(group.authorID) else false,
            ) {
                group.chats.forEach { chat ->
                    val showTimeLabel = group.lastChatID == chat.id

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isInteractive && group.isMe && group.lastChatID != null) {
                            ChatReadReceipt(
                                readCount = readCount(chat, room.participants, taxiUser?.oid),
                                showTime = showTimeLabel,
                                time = group.time,
                                alignment = Alignment.End //isMe
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalAlignment = if (group.isMe) Alignment.End else Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ChatBubble(
                                chat = chat,
                                group = group,
                                room = room,
                                isInteractive = isInteractive,
                                isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable,
                                onImageClick = onImageClick,
                                onCommitPayment = onCommitPayment
                            )
                        }

                        if (group.lastChatID != null) {
                            if (isInteractive && !group.isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                ChatReadReceipt(
                                    readCount = readCount(chat, room.participants, taxiUser?.oid),
                                    showTime = showTimeLabel,
                                    time = group.time,
                                    alignment = Alignment.Start
                                )
                            } else if (!isInteractive && showTimeLabel) {
                                Spacer(modifier = Modifier.width(4.dp))
                                ChatReadReceipt(
                                    readCount = 3,
                                    showTime = true,
                                    time = group.time,
                                    alignment = Alignment.Start
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun readCount(
    chat: TaxiChat,
    participants: List<TaxiParticipant>,
    myId: String?
): Int {
    val otherParticipants = participants.filter { it.id != myId }
    return otherParticipants.count { participant ->
        participant.readAt <= chat.time
    }
}

@Composable
private fun ChatBubble(
    chat: TaxiChat,
    group: TaxiChatGroup,
    room: TaxiRoom,
    isInteractive: Boolean,
    isCommitPaymentAvailable: Boolean,
    onImageClick: (String) -> Unit,
    onCommitPayment: () -> Unit
) {
    when (chat.type) {
        TaxiChat.ChatType.IN, TaxiChat.ChatType.OUT ->
            ChatGeneralMessage(authorName = chat.authorName, type = chat.type)

        TaxiChat.ChatType.TEXT ->
            TaxiChatBubble(
                content = chat.content,
                showTip = group.lastChatID == chat.id,
                isMe = if (isInteractive) group.isMe else false
            )

        TaxiChat.ChatType.S3IMG ->
            ChatImageBubble(id = chat.content) {
                if (isInteractive) onImageClick(chat.content)
            }

        TaxiChat.ChatType.DEPARTURE ->
            ChatDepartureBubble(room = if (isInteractive) room else TaxiRoom.mock())

        TaxiChat.ChatType.ARRIVAL -> ChatArrivalBubble()
        TaxiChat.ChatType.SETTLEMENT -> ChatSettlementBubble()
        TaxiChat.ChatType.PAYMENT -> ChatPaymentBubble()

        TaxiChat.ChatType.ACCOUNT ->
            ChatAccountBubble(
                content = if (isInteractive) chat.content else "BANK NUMBER",
                isCommitPaymentAvailable = if (isInteractive) isCommitPaymentAvailable else true
            ) {
                if (isInteractive) onCommitPayment()
            }

        TaxiChat.ChatType.SHARE ->
            ChatShareBubble(room = if (isInteractive) room else TaxiRoom.mock())

        else -> Text(chat.type.name)
    }
}