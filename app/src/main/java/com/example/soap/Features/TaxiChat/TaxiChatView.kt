package com.example.soap.Features.TaxiChat

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.FullscreenImage.FullscreenImageView
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiChat.Components.TaxiArrivalBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatAccountBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatDayMessage
import com.example.soap.Features.TaxiChat.Components.TaxiChatGeneralMessage
import com.example.soap.Features.TaxiChat.Components.TaxiChatImageBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatPaymentBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatSettlementBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatShareBubble
import com.example.soap.Features.TaxiChat.Components.TaxiChatUserWrapper
import com.example.soap.Features.TaxiChat.Components.TaxiChatViewNavigationBar
import com.example.soap.Features.TaxiChat.Components.TaxiDepartureBubble
import com.example.soap.R
import com.example.soap.Shared.Extensions.formattedTime
import com.example.soap.Shared.Extensions.toLocalDate
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModelMocks.MockTaxiChatViewModel
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TaxiChatView(
    viewModel: TaxiChatViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val groupedChats by viewModel.groupedChats.collectAsState(initial = emptyList())
    val taxiUser by viewModel.taxiUser.collectAsState()

    var text by remember { mutableStateOf("") }
    var tappedImageId by remember { mutableStateOf<String?>(null) }
    var showCallTaxiAlert by remember { mutableStateOf(false) }
    var showErrorAlert by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotosPicker by remember { mutableStateOf(false) }

    val topChatID = remember { mutableStateOf<String?>(null) }
    val isLoadingMore = remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val backStackEntry = navController.currentBackStackEntry!!
    val room = viewModel.room.collectAsState().value
    LaunchedEffect(room.id) {
        try {
            val json = Gson().toJson(room)
            backStackEntry.savedStateHandle["room_json"] = json
            viewModel.switchRoom(room)
            viewModel.fetchInitialChats()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load chats"
            showErrorAlert = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            val bitmap = context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            selectedImage = bitmap
        }
    }


    Scaffold(
        topBar = {
            TaxiChatViewNavigationBar(
                room = room,
                onDismiss = { navController.navigate(Channel.TaxiChatListView.name) },
                onClickCallTaxi = { showCallTaxiAlert = true },
                onClickLeave = {
                    coroutineScope.launch { viewModel.leaveRoom() }
                },
                isEnabled = viewModel.isLeaveRoomAvailable
            )
        },

        bottomBar = {
            InputBar(
                text = text,
                onTextChange = { text = it },
                selectedImage = selectedImage,
                onRemoveImage = { selectedImage = null },
                onSendText = { message ->
                    coroutineScope.launch {
                        viewModel.sendChat(message, TaxiChat.ChatType.TEXT)
                        text = ""
                    }
                },
                onSendImage = { image ->
                    coroutineScope.launch {
                        try {
                            isUploading = true
                            viewModel.sendImage(image)
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Unknown error"
                            showErrorAlert = true
                        } finally {
                            isUploading = false
                            selectedImage = null
                        }
                    }
                },
                isUploading = isUploading,
                isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable,
                isCommitSettlementAvailable = viewModel.isCommitSettlementAvailable,
                onCommitPayment = {
                    coroutineScope.launch {
                        viewModel.commitPayment()
                    }
                },
                onCommitSettlement = {
                    coroutineScope.launch {
                        viewModel.commitSettlement()
                    }
                },
                onPickPhoto = { showPhotosPicker = true },
                taxiUser = taxiUser
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
                is TaxiChatViewModel.ViewState.Loading -> LoadingView()
                is TaxiChatViewModel.ViewState.Error -> ErrorView(
                    icon = Icons.Default.Warning,
                    errorMessage = (state as TaxiChatViewModel.ViewState.Error).message,
                    onRetry = { coroutineScope.launch { viewModel.fetchInitialChats() } }
                )

                is TaxiChatViewModel.ViewState.Loaded -> ContentView(
                    groupedChats = groupedChats,
                    viewModel = viewModel,
                    onImageClick = { tappedImageId = it },
                    topChatID = topChatID,
                    isLoadingMore = isLoadingMore,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    if (tappedImageId != null) {
        FullscreenImageView(
            id = tappedImageId,
            onDismiss = { tappedImageId = null }
        )
    }

    if (showCallTaxiAlert) {
        AlertDialog(
            onDismissRequest = { showCallTaxiAlert = false },
            title = { Text("Call Taxi") },
            text = {
                Text(
                    "Launch the taxi app with departure/destination set. " +
                            "Once everyone has gathered, press to call a taxi from ${room.source.title} → ${room.destination.title}."
                )
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        openKakaoT(
                            context,
                            viewModel
                        )
                    }) { Text("Open Kakao T") }
                    TextButton(onClick = {
                        openUber(
                            context,
                            viewModel
                        )
                    }) { Text("Open Uber") }
                    TextButton(onClick = { showCallTaxiAlert = false }) { Text("Cancel") }
                }
            }
        )
    }

    if (showErrorAlert) {
        AlertDialog(
            onDismissRequest = { showErrorAlert = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showErrorAlert = false
                }) { Text("Okay") }
            }
        )
    }

    if (showPhotosPicker) {
        launcher.launch("image/*")
        showPhotosPicker = false
    }
}

// MARK: - Views
@Composable
fun ContentView(
    groupedChats: List<TaxiChatGroup>,
    viewModel: TaxiChatViewModelProtocol,
    onImageClick: (String) -> Unit,
    topChatID: MutableState<String?>,
    isLoadingMore: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val taxiUser by viewModel.taxiUser.collectAsState(initial = null)
    val listState = rememberLazyListState()

    LaunchedEffect(groupedChats.map { it.chats.lastOrNull()?.id }) {
        val lastIndex = groupedChats.lastIndex
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
    ) {
//        item {
//            Spacer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .onGloballyPositioned {
//                        loadMoreIfNeeded(viewModel, topChatID, isLoadingMore, coroutineScope)
//                    }
//            )
//        }
        val flattenedChats = groupedChats.flatMap { group ->
            group.chats.map { chat -> chat to group }
        }
        items(flattenedChats, key = { (chat, _) -> chat.id }) { (chat, group) ->
            val showTimeLabel = group.lastChatID == chat.id
            val otherParticipants =
                viewModel.room.collectAsState().value.participants.filter { it.id != taxiUser?.oid }
            val readCount = otherParticipants.count { it.readAt <= chat.time }

            TaxiChatUserWrapper(
                authorID = group.authorID,
                authorName = group.authorName,
                authorProfileImageURL = group.authorProfileURL,
                date = group.time,
                isMe = group.isMe,
                isGeneral = group.isGeneral,
                isWithdrawn = group.authorIsWithdrew ?: false
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (group.isMe && group.lastChatID != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            if (readCount > 0) Text(
                                "$readCount",
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (showTimeLabel) Text(
                                group.time.formattedTime(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth(if (group.isMe) Alignment.End else Alignment.Start)
                    ) {
                        when (chat.type) {
                            TaxiChat.ChatType.IN, TaxiChat.ChatType.OUT ->
                                TaxiChatGeneralMessage(
                                    authorName = chat.authorName,
                                    type = chat.type
                                )

                            TaxiChat.ChatType.TEXT ->
                                TaxiChatBubble(
                                    content = chat.content,
                                    showTip = showTimeLabel,
                                    isMe = group.isMe
                                )

                            TaxiChat.ChatType.S3IMG ->
                                TaxiChatImageBubble(id = chat.content) { onImageClick(chat.content) }

                            TaxiChat.ChatType.DEPARTURE ->
                                TaxiDepartureBubble(room = viewModel.room.collectAsState().value)

                            TaxiChat.ChatType.ARRIVAL -> TaxiArrivalBubble()
                            TaxiChat.ChatType.SETTLEMENT -> TaxiChatSettlementBubble()
                            TaxiChat.ChatType.PAYMENT -> TaxiChatPaymentBubble()
                            TaxiChat.ChatType.ACCOUNT ->
                                TaxiChatAccountBubble(
                                    content = chat.content,
                                    isCommitPaymentAvailable = viewModel.isCommitPaymentAvailable
                                ) {
                                    coroutineScope.launch {
                                        try {
                                            viewModel.commitPayment()
                                        } catch (e: Exception) {
                                            Log.e("TaxiChatView", "Failed to commit payment", e)
                                        }
                                    }
                                }

                            TaxiChat.ChatType.SHARE -> TaxiChatShareBubble() {}
                            else -> Text(chat.type.name)
                        }
                    }

                    if (!group.isMe && group.lastChatID != null) {
                        Column(horizontalAlignment = Alignment.Start) {
                            if (readCount > 0) Text(
                                "$readCount",
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (showTimeLabel) Text(
                                group.time.formattedTime(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item { Spacer(modifier = Modifier.height(1.dp)) }

        val firstDate = TaxiChatGroup.mockList().firstOrNull()?.time
        firstDate?.let { date ->
            item { TaxiChatDayMessage(date = date.toLocalDate()) }
        }

        items(TaxiChatGroup.mockList().take(6)) { groupedChat ->
            TaxiChatUserWrapper(
                authorID = groupedChat.authorID,
                authorName = groupedChat.authorName,
                authorProfileImageURL = groupedChat.authorProfileURL,
                date = groupedChat.time,
                isMe = false,
                isGeneral = groupedChat.isGeneral,
                isWithdrawn = groupedChat.authorIsWithdrew ?: false
            ) {
                groupedChat.chats.forEach { chat ->
                    val showTimeLabel = groupedChat.lastChatID == chat.id
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentWidth()
                        ) {
                            when (chat.type) {
                                TaxiChat.ChatType.IN, TaxiChat.ChatType.OUT ->
                                    TaxiChatGeneralMessage(authorName = chat.authorName, type = chat.type)
                                TaxiChat.ChatType.TEXT ->
                                    TaxiChatBubble(content = chat.content, showTip = showTimeLabel, isMe = false)
                                TaxiChat.ChatType.DEPARTURE -> TaxiDepartureBubble(room = TaxiRoom.mock())
                                TaxiChat.ChatType.ARRIVAL -> TaxiArrivalBubble()
                                TaxiChat.ChatType.SETTLEMENT -> TaxiChatSettlementBubble()
                                TaxiChat.ChatType.PAYMENT -> TaxiChatPaymentBubble()
                                TaxiChat.ChatType.ACCOUNT -> TaxiChatAccountBubble(content = "BANK NUMBER", isCommitPaymentAvailable = true) {}
                                TaxiChat.ChatType.SHARE -> TaxiChatShareBubble() {}
                                else -> Text(chat.type.name)
                            }}
                        if (showTimeLabel) {
                            Column(horizontalAlignment = Alignment.Start,
                                modifier = Modifier.wrapContentWidth()) {
                                Text("3", style = MaterialTheme.typography.labelSmall)
                                Text(groupedChat.time.formattedTime(), style = MaterialTheme.typography.labelSmall, modifier = Modifier.wrapContentWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    selectedImage: Bitmap?,
    onRemoveImage: () -> Unit,
    onSendText: (String) -> Unit,
    onSendImage: (Bitmap) -> Unit,
    isUploading: Boolean,
    isCommitPaymentAvailable: Boolean,
    isCommitSettlementAvailable: Boolean,
    onCommitPayment: () -> Unit,
    onCommitSettlement: () -> Unit,
    onPickPhoto: () -> Unit,
    taxiUser: TaxiUser?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Add, contentDescription = "More")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        onCommitPayment()
                        expanded = false
                    },
                    enabled = isCommitPaymentAvailable,
                    text = { Text("Send Payment") }
                )

                DropdownMenuItem(
                    onClick = {
                        onCommitSettlement()
                        expanded = false
                    },
                    enabled = isCommitSettlementAvailable,
                    text = { Text("Request Settlement") }
                )

                DropdownMenuItem(
                    onClick = {
                        onPickPhoto()
                        expanded = false
                    },
                    text = { Text("Photo Library") }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (selectedImage != null) {
                Box {
                    Image(
                        bitmap = selectedImage.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    IconButton(
                        onClick = onRemoveImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White
                        )
                    }
                }
            } else {
                //Text input
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Chat as ${taxiUser?.nickname ?: "unknown"}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send button
        Button(
            onClick = {
                if (selectedImage != null) {
                    onSendImage(selectedImage)
                    onRemoveImage()
                } else if (text.isNotBlank()) {
                    onSendText(text)
                    onTextChange("")
                }
            },
            enabled = text.isNotBlank() || selectedImage != null
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.paperplane),
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp),
                    tint = if (text.isNotBlank() || selectedImage != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// MARK: - Functions
fun loadMoreIfNeeded(
    viewModel: TaxiChatViewModelProtocol,
    topChatID: MutableState<String?>,
    isLoadingMore: MutableState<Boolean>,
    coroutineScope: CoroutineScope
) {
    if (isLoadingMore.value) return
    val oldestDate = viewModel.groupedChats.value.firstOrNull()?.chats?.firstOrNull()?.time ?: return
    if (viewModel.fetchedDateSet.contains(oldestDate)) return

    topChatID.value = viewModel.groupedChats.value.firstOrNull()?.id
    viewModel.fetchedDateSet.add(oldestDate)
    isLoadingMore.value = true

    coroutineScope.launch {
        viewModel.fetchChats(before = oldestDate)
        isLoadingMore.value = false
    }
}

fun openKakaoT(context: Context, viewModel: TaxiChatViewModelProtocol) {
    val url = "kakaot://taxi/set?" +
            "dest_lng=${viewModel.room.value.destination.longitude}&dest_lat=${viewModel.room.value.destination.latitude}" +
            "&origin_lng=${viewModel.room.value.source.longitude}&origin_lat=${viewModel.room.value.source.latitude}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.taxi"))
        context.startActivity(marketIntent)
    }
}

fun openUber(context: Context, viewModel: TaxiChatViewModelProtocol) {
    val url = "uber://?action=setPickup&client_id=a" +
            "&pickup[latitude]=${viewModel.room.value.source.latitude}&pickup[longitude]=${viewModel.room.value.source.longitude}" +
            "&dropoff[latitude]=${viewModel.room.value.destination.latitude}&dropoff[longitude]=${viewModel.room.value.destination.longitude}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.ubercab"))
        context.startActivity(marketIntent)
    }
}


// MARK: - Previews
@Preview
@Composable
private fun PreviewTaxiChatViewLoading() {
    val viewModel = remember {
        MockTaxiChatViewModel(
            initialState = TaxiChatViewModel.ViewState.Loading
        )
    }
    Theme{
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview
@Composable
fun PreviewTaxiChatViewLoaded() {
    val viewModel = remember {
        MockTaxiChatViewModel(
            initialState = TaxiChatViewModel.ViewState.Loaded(TaxiChatGroup.mockList()),
            initialGroupedChats = TaxiChatGroup.mockList(),
            initialTaxiUser = TaxiUser.mock()
        )
    }
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview
@Composable
fun PreviewTaxiChatViewError() {
    val viewModel = remember {
        MockTaxiChatViewModel(
            initialState = TaxiChatViewModel.ViewState.Error("Failed to load chats.")
        )
    }
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}

@Preview
@Composable
fun PreviewTaxiChatViewEmpty() {
    val viewModel = remember {
        MockTaxiChatViewModel(
            initialState = TaxiChatViewModel.ViewState.Loaded(emptyList()),
            initialGroupedChats = emptyList(),
            initialTaxiUser = TaxiUser.mock()
        )
    }
    Theme {
        TaxiChatView(viewModel = viewModel, navController = rememberNavController())
    }
}
