package org.sparcs.soap.App.Features.TaxiChat

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.TaxiDeepLinkHelper
import org.sparcs.soap.App.Domain.Models.Taxi.ChatRenderItem
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Features.FullscreenImage.FullscreenImageView
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiChat.ChatBubbles.ChatCollectionView
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatBubblePositionResolver
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatRenderItemBuilder
import org.sparcs.soap.App.Features.TaxiChat.Components.DefaultMessagePresentationPolicy
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiChatInputBar
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiChatViewNavigationBar
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiGroupingPolicy
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.openUri
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.GlobalAlertDialog
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.Taxi.PreviewTaxiChatViewModel
import org.sparcs.soap.R

@Composable
fun TaxiChatView(
    viewModel: TaxiChatViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val taxiUser by viewModel.taxiUser.collectAsState()
    val room by viewModel.room.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var text by remember { mutableStateOf("") }
    var showCallTaxiAlert by remember { mutableStateOf(false) }
    var showPayMoneyAlert by remember { mutableStateOf(false) }
    var tappedImageID by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setup()
        viewModel.fetchInitialChats()
    }

    Scaffold(
        topBar = {
            TaxiChatViewNavigationBar(
                room = room,
                myUserId = taxiUser?.oid,
                onDismiss = { navController.popBackStack() },
                onClickCallTaxi = { showCallTaxiAlert = true },
                onReport = {
                    val json = Uri.encode(Gson().toJson(room))
                    navController.navigate("${Channel.TaxiReportView.name}?room_json=$json")
                },
                onClickLeave = {
                    coroutineScope.launch {
                        try {
                            viewModel.leaveRoom()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            viewModel.isAlertPresented = true
                        }
                    }
                },
                onCarrierToggle = { coroutineScope.launch { viewModel.toggleCarrier(it) } },
                onArrivalToggle = { coroutineScope.launch { viewModel.updateArrival(it) } },
                isLeaveAvailable = viewModel.isLeaveRoomAvailable
            )
        },
        bottomBar = {
            TaxiChatInputBar(
                text = text,
                onTextChange = { text = it },
                taxiUser = taxiUser,
                isUploading = isUploading,
                isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable,
                isCommitSettlementAvailable = viewModel.isCommitSettlementAvailable,
                onSendText = { message ->
                    coroutineScope.launch { viewModel.sendChat(message, TaxiChat.ChatType.TEXT) }
                },
                onSendImage = { bitmap ->
                    coroutineScope.launch { viewModel.sendImage(bitmap) }
                },
                onCommitSettlement = {
                    coroutineScope.launch { viewModel.commitSettlement() }
                },
                onCommitPayment = { showPayMoneyAlert = true }
            )
        },
        modifier = Modifier.analyticsScreen("Taxi Chat")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Crossfade(
                targetState = state,
                animationSpec = tween(300),
                label = "StateTransition"
            ) { currentState ->
                when (currentState) {
                    is TaxiChatViewModel.ViewState.Loading -> {
                        ChatCollectionView(
                            items = PlaceholderItems,
                            room = room,
                            user = null,
                            onImageClick = {},
                            onCommitPayment = {},
                            listState = rememberLazyListState(),
                            scrollToBottomTrigger = 0,
                            modifier = Modifier.alpha(0.5f)
                        )
                    }

                    is TaxiChatViewModel.ViewState.Loaded -> {
                        ChatCollectionView(
                            items = viewModel.renderItems.collectAsState().value,
                            room = room,
                            user = taxiUser,
                            onImageClick = { tappedImageID = it },
                            onCommitPayment = { showPayMoneyAlert = true },
                            listState = listState,
                            scrollToBottomTrigger = viewModel.scrollToBottomTrigger
                        )
                    }

                    is TaxiChatViewModel.ViewState.Error -> {
                        ErrorView(
                            message = currentState.message,
                            onRetry = { coroutineScope.launch { viewModel.fetchInitialChats() } }
                        )
                    }
                }
            }
        }
    }

    if (showCallTaxiAlert) {
        AlertDialog(
            onDismissRequest = { showCallTaxiAlert = false },
            title = { Text(stringResource(R.string.call_taxi)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            R.string.taxi_launch_info,
                            room.source.title,
                            room.destination.title
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getKakaoTUri(room.source, room.destination)
                            context.openUri(uri, "com.kakao.taxi")
                            showCallTaxiAlert = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.open_kakao_t))
                    }

                    OutlinedButton(
                        onClick = {
                            val uberUri = TaxiDeepLinkHelper.getUberUri(room.source, room.destination)
                            context.openUri(uberUri, "com.ubercab")
                            showCallTaxiAlert = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.open_uber))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallTaxiAlert = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showPayMoneyAlert) {
        AlertDialog(
            onDismissRequest = { showPayMoneyAlert = false },
            title = { Text(stringResource(R.string.send_payment)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.payment_send_instructions))
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getKakaoPayUri(context, viewModel.account)
                            context.openUri(uri, "com.kakao.talk")
                            showPayMoneyAlert = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.open_kakao_pay))
                    }
                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getTossUri(viewModel.account)
                            context.openUri(uri, "viva.republica.toss")
                            showPayMoneyAlert = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.open_toss))
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch { viewModel.commitPayment() }
                            showPayMoneyAlert = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.already_sent))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPayMoneyAlert = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )

    if (tappedImageID != null) {
        FullscreenImageView(
            id = tappedImageID!!,
            onDismiss = { tappedImageID = null }
        )
    }
}

private val PlaceholderItems: List<ChatRenderItem> by lazy {
    val builder = ChatRenderItemBuilder(
        policy = TaxiGroupingPolicy(),
        positionResolver = ChatBubblePositionResolver(),
        presentationPolicy = DefaultMessagePresentationPolicy()
    )
    builder.build(chats = TaxiChat.mockList().take(11), myUserID = null)
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun TaxiChatView_Loading_Preview() {
    val viewModel = PreviewTaxiChatViewModel(
        initialState = TaxiChatViewModel.ViewState.Loading
    )
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Loaded State")
@Composable
private fun TaxiChatView_Loaded_Preview() {
    val viewModel = PreviewTaxiChatViewModel(
        initialState = TaxiChatViewModel.ViewState.Loaded
    )
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun TaxiChatView_Error_Preview() {
    val viewModel = PreviewTaxiChatViewModel(
        initialState = TaxiChatViewModel.ViewState.Error("Something went wrong")
    )
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}