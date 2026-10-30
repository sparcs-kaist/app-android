package org.sparcs.soap.app.features.taxiChat

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.models.taxi.ridersWithOwnStatus
import org.sparcs.soap.app.features.fullscreenImage.FullscreenImageView
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiChat.components.CallTaxiDialog
import org.sparcs.soap.app.features.taxiChat.components.SendPaymentDialog
import org.sparcs.soap.app.features.taxiChat.components.TaxiChatInputBar
import org.sparcs.soap.app.features.taxiChat.components.TaxiChatViewNavigationBar
import org.sparcs.soap.app.features.taxiChat.components.TaxiSettlementAmountSheet
import org.sparcs.soap.app.features.taxiChat.rideInfo.TaxiRideInfoPresentation
import org.sparcs.soap.app.features.taxiChat.rideInfo.TaxiRideInfoView
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModel
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModelProtocol
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomList
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomListSkeleton
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.rememberTaxiRoomShare
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiChatListViewModel
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiChatViewModel
import java.util.Date

@Composable
fun TaxiChatView(
    viewModel: TaxiChatViewModelProtocol = hiltViewModel<TaxiChatViewModel>(),
    listViewModel: TaxiChatListViewModelProtocol = hiltViewModel<TaxiChatListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val taxiUser by viewModel.taxiUser.collectAsState()
    val room by viewModel.room.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isArrived by viewModel.isArrived.collectAsState()
    val hasCarrier by viewModel.hasCarrier.collectAsState()
    val riders = room.ridersWithOwnStatus(taxiUser?.oid, isArrived, hasCarrier)

    var text by remember { mutableStateOf("") }
    var showCallTaxiAlert by remember { mutableStateOf(false) }
    var showPayMoneyAlert by remember { mutableStateOf(false) }
    var showSettlementAmountDialog by remember { mutableStateOf(false) }
    var showsRideInfo by remember { mutableStateOf(false) }
    var tappedImageID by remember { mutableStateOf<String?>(null) }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val tipStore = LocalTipStore.current
    val shareRoom = rememberTaxiRoomShare(room)

    fun dismissCallTaxiAlert() { showCallTaxiAlert = false }
    fun dismissPayMoneyAlert() { showPayMoneyAlert = false }
    fun resetSettlementDialog() { showSettlementAmountDialog = false }
    fun dismissFullscreenImage() { tappedImageID = null }

    fun openRideInfo() {
        tipStore?.invalidate(BuddyTip.RideInfo)
        showsRideInfo = true
    }

    fun openReport() {
        showsRideInfo = false
        val json = Uri.encode(Gson().toJson(room))
        navController.navigate("${Channel.TaxiReportView.name}?room_json=$json")
    }

    fun leaveRoom() {
        coroutineScope.launch {
            if (viewModel.leaveRoom()) {
                showsRideInfo = false
                navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setup()
        viewModel.fetchInitialChats()
        listViewModel.fetchData()
    }

    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(room.departAt) {
        val delayMs = room.departAt.time - Date().time
        if (delayMs > 0) {
            delay(delayMs + 1000)
            now = Date()
        }
    }

    val chatListContent = @Composable {
        ModalDrawerSheet(
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            drawerShape = RectangleShape,
            modifier = Modifier.width(360.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val listStateValue by listViewModel.state.collectAsState()
                Text(
                    text = stringResource(R.string.active_groups),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                when (val listState = listStateValue) {
                    is TaxiChatListViewModel.ViewState.Loading -> TaxiChatRoomListSkeleton()
                    is TaxiChatListViewModel.ViewState.Loaded -> {
                        TaxiChatRoomList(
                            onGoing = listState.onGoing,
                            done = listState.done,
                            taxiUser = viewModel.taxiUser.collectAsState().value,
                            onRoomClick = { selectedRoom ->
                                coroutineScope.launch { drawerState.close() }
                                val json = Uri.encode(Gson().toJson(selectedRoom))
                                navController.navigate(Channel.TaxiChatView.name + "?room_json=$json") {
                                    popUpTo(Channel.TaxiChatView.name + "?room_json={room_json}") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    is TaxiChatListViewModel.ViewState.Error -> {
                        ErrorView(error = listState.error, onRetry = { listViewModel.fetchData() })
                    }
                }
            }
        }
    }

    val mainScaffold = @Composable {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                TaxiChatViewNavigationBar(
                    room = room,
                    riders = riders,
                    isCompactHeight = isLandscape,
                    onDismiss = { navController.popBackStack() },
                    onOpenRideInfo = ::openRideInfo
                )
            },
            bottomBar = {
                // Nothing to send while the chat failed to load.
                if (state !is TaxiChatViewModel.ViewState.Error) Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = if (isLandscape) Modifier.widthIn(max = 1000.dp)
                        else Modifier.fillMaxWidth()
                    ) {
                        TaxiChatInputBar(
                            text = text,
                            onTextChange = { text = it },
                            taxiUser = taxiUser,
                            isUploading = isUploading,
                            isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable,
                            isCommitSettlementAvailable = viewModel.isCommitSettlementAvailable,
                            onSendText = { message ->
                                viewModel.sendChat(message, TaxiChat.ChatType.TEXT)
                            },
                            onSendImage = { bitmap ->
                                coroutineScope.launch { viewModel.sendImage(bitmap) }
                            },
                            onCommitSettlement = { showSettlementAmountDialog = true },
                            onCommitPayment = { showPayMoneyAlert = true }
                        )
                    }
                }
            },
            modifier = Modifier
                .navigationBarsPadding()
                .analyticsScreen("Taxi Chat")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TaxiChatPortraitLayout(
                        state = state,
                        room = room,
                        taxiUser = taxiUser,
                        listState = listState,
                        viewModel = viewModel,
                        onImageClick = { tappedImageID = it },
                        onPayMoneyClick = { showPayMoneyAlert = true },
                        coroutineScope = coroutineScope
                    )
                }
            }
        }
    }

    if (isLandscape) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { chatListContent() },
            gesturesEnabled = true,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
            modifier = Modifier.clipToBounds()
        ) {
            mainScaffold()
        }
    } else {
        mainScaffold()
    }

    if (showsRideInfo) {
        TaxiRideInfoPresentation(onDismiss = { showsRideInfo = false }) {
            TaxiRideInfoView(
                room = room,
                riders = riders,
                myUserID = taxiUser?.oid,
                isArrived = isArrived,
                hasCarrier = hasCarrier,
                canMarkArrival = viewModel.isLeaveRoomAvailable && now.before(room.departAt),
                canLeave = viewModel.isLeaveRoomAvailable,
                onToggleArrival = { viewModel.updateArrival(!isArrived) },
                onToggleCarrier = { viewModel.toggleCarrier(!hasCarrier) },
                onShare = shareRoom,
                onCallTaxi = { showCallTaxiAlert = true },
                onReport = ::openReport,
                onLeave = ::leaveRoom
            )
        }
    }

    if (showCallTaxiAlert) {
        CallTaxiDialog(room = room, onDismiss = ::dismissCallTaxiAlert)
    }

    if (showPayMoneyAlert) {
        SendPaymentDialog(
            account = viewModel.account,
            onAlreadySent = { coroutineScope.launch { viewModel.commitPayment() } },
            onDismiss = ::dismissPayMoneyAlert
        )
    }

    if (showSettlementAmountDialog) {
        TaxiSettlementAmountSheet(
            participantCount = room.participants.size,
            onDismiss = ::resetSettlementDialog,
            onCommit = viewModel::commitSettlement,
        )
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )

    if (tappedImageID != null) {
        FullscreenImageView(id = tappedImageID!!, onDismiss = { dismissFullscreenImage() })
    }
}

@Composable
private fun TaxiChatPortraitLayout(
    state: TaxiChatViewModel.ViewState,
    room: TaxiRoom,
    taxiUser: TaxiUser?,
    listState: LazyListState,
    viewModel: TaxiChatViewModelProtocol,
    onImageClick: (String) -> Unit,
    onPayMoneyClick: () -> Unit,
    coroutineScope: CoroutineScope
) {
    Crossfade(
        targetState = state,
        animationSpec = tween(300),
        label = "StateTransition"
    ) { currentState ->
        when (currentState) {
            is TaxiChatViewModel.ViewState.Loading -> {
                ChatLoadingSkeleton()
            }

            is TaxiChatViewModel.ViewState.Loaded -> {
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.maxOfOrNull { it.index } ?: 0
                        val totalCount = listState.layoutInfo.totalItemsCount
                        totalCount > 0 && lastVisibleIndex >= totalCount - 3
                    }
                }

                LaunchedEffect(Unit) {
                    snapshotFlow { shouldLoadMore }.collect { loadMore ->
                            if (loadMore) { viewModel.loadMoreChats() }
                        }
                }

                ChatCollectionView(
                    items = viewModel.renderItems.collectAsState().value,
                    room = room,
                    user = taxiUser,
                    onImageClick = onImageClick,
                    onCommitPayment = onPayMoneyClick,
                    listState = listState,
                    scrollToBottomTrigger = viewModel.scrollToBottomTrigger
                )
            }

            is TaxiChatViewModel.ViewState.Error -> {
                ErrorView(
                    error = currentState.error,
                    onRetry = { coroutineScope.launch { viewModel.fetchInitialChats() } }
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Loading State")
@Composable
private fun TaxiChatView_Loading_Preview() {
    val viewModel = PreviewTaxiChatViewModel(initialState = TaxiChatViewModel.ViewState.Loading)
    Theme {
        TaxiChatView(
            viewModel = viewModel,
            listViewModel = PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Loading),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Loaded State Landscape", widthDp = 1200, heightDp = 800)
@Composable
private fun TaxiChatView_Loaded_Landscape_Preview() {
    val viewModel = PreviewTaxiChatViewModel(initialState = TaxiChatViewModel.ViewState.Loaded)
    Theme {
        TaxiChatView(
            viewModel = viewModel,
            listViewModel = PreviewTaxiChatListViewModel(
                TaxiChatListViewModel.ViewState.Loaded(
                    TaxiRoom.mockList().subList(0, 3),
                    TaxiRoom.mockList().subList(3, 5)
                )
            ),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Loaded State")
@Composable
private fun TaxiChatView_Loaded_Preview() {
    val viewModel = PreviewTaxiChatViewModel(initialState = TaxiChatViewModel.ViewState.Loaded)
    Theme {
        TaxiChatView(
            viewModel = viewModel,
            listViewModel = PreviewTaxiChatListViewModel(
                TaxiChatListViewModel.ViewState.Loaded(
                    TaxiRoom.mockList().subList(0, 3),
                    TaxiRoom.mockList().subList(3, 5)
                )
            ),
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun TaxiChatView_Error_Preview() {
    val viewModel = PreviewTaxiChatViewModel(initialState = TaxiChatViewModel.ViewState.Error(Exception()))
    Theme {
        TaxiChatView(
            viewModel = viewModel,
            listViewModel = PreviewTaxiChatListViewModel(TaxiChatListViewModel.ViewState.Error(Exception())),
            navController = rememberNavController()
        )
    }
}
