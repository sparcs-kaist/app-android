package org.sparcs.soap.app.features.taxiChat

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import org.sparcs.soap.app.domain.helpers.TaxiDeepLinkHelper
import org.sparcs.soap.app.domain.models.taxi.ChatRenderItem
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.features.fullscreenImage.FullscreenImageView
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiChat.components.ChatBubblePositionResolver
import org.sparcs.soap.app.features.taxiChat.components.ChatRenderItemBuilder
import org.sparcs.soap.app.features.taxiChat.components.DefaultMessagePresentationPolicy
import org.sparcs.soap.app.features.taxiChat.components.TaxiChatInputBar
import org.sparcs.soap.app.features.taxiChat.components.TaxiChatViewNavigationBar
import org.sparcs.soap.app.features.taxiChat.components.TaxiGroupingPolicy
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModel
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModelProtocol
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomList
import org.sparcs.soap.app.features.taxiChatList.components.TaxiChatRoomListSkeleton
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.openUri
import org.sparcs.soap.app.shared.mocks.taxi.mockList
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

    var text by remember { mutableStateOf("") }
    var showCallTaxiAlert by remember { mutableStateOf(false) }
    var showPayMoneyAlert by remember { mutableStateOf(false) }
    var showSettlementAmountDialog by remember { mutableStateOf(false) }
    var settlementAmountText by remember { mutableStateOf("") }
    var tappedImageID by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun dismissCallTaxiAlert() { showCallTaxiAlert = false }
    fun dismissPayMoneyAlert() { showPayMoneyAlert = false }
    fun resetSettlementDialog() {
        showSettlementAmountDialog = false
        settlementAmountText = ""
    }
    fun dismissFullscreenImage() { tappedImageID = null }

    LaunchedEffect(Unit) {
        viewModel.setup()
        viewModel.fetchInitialChats()
        listViewModel.fetchData()
    }

    // Trigger recomposition when departure time is reached
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
                            val success = viewModel.leaveRoom()
                            if (success) { navController.popBackStack() }
                        }
                    },
                    onCarrierToggle = { viewModel.toggleCarrier(it) },
                    onArrivalToggle = { viewModel.updateArrival(it) },
                    isLeaveAvailable = viewModel.isLeaveRoomAvailable
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
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

    if (showCallTaxiAlert) {
        AlertDialog(
            onDismissRequest = { dismissCallTaxiAlert() },
            title = { Text(stringResource(R.string.call_taxi)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.taxi_launch_info, room.source.title, room.destination.title))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getKakaoTUri(room.source, room.destination)
                            context.openUri(uri, "com.kakao.taxi")
                            dismissCallTaxiAlert()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.open_kakao_t)) }

                    OutlinedButton(
                        onClick = {
                            val uberUri = TaxiDeepLinkHelper.getUberUri(room.source, room.destination)
                            context.openUri(uberUri, "com.ubercab")
                            dismissCallTaxiAlert()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.open_uber)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { dismissCallTaxiAlert() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (showPayMoneyAlert) {
        AlertDialog(
            onDismissRequest = { dismissPayMoneyAlert() },
            title = { Text(stringResource(R.string.send_payment)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.payment_send_instructions))
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getKakaoPayUri(context, viewModel.account)
                            context.openUri(uri, "com.kakao.talk")
                            dismissPayMoneyAlert()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.open_kakao_pay)) }
                    OutlinedButton(
                        onClick = {
                            val uri = TaxiDeepLinkHelper.getTossUri(viewModel.account)
                            context.openUri(uri, "viva.republica.toss")
                            dismissPayMoneyAlert()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.open_toss)) }
                    Button(
                        onClick = {
                            coroutineScope.launch { viewModel.commitPayment() }
                            dismissPayMoneyAlert()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.already_sent)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { dismissPayMoneyAlert() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (showSettlementAmountDialog) {
        AlertDialog(
            onDismissRequest = { resetSettlementDialog() },
            title = { Text(stringResource(R.string.enter_settlement_amount)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = settlementAmountText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) { settlementAmountText = it }
                        },
                        label = { Text(stringResource(R.string.settlement_amount_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text(stringResource(R.string.currency_unit)) }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = settlementAmountText.toIntOrNull()
                        if (amount != null && amount > 0) {
                            viewModel.commitSettlement(amount)
                            resetSettlementDialog()
                        }
                    },
                    enabled = settlementAmountText.isNotBlank()
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { resetSettlementDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
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
