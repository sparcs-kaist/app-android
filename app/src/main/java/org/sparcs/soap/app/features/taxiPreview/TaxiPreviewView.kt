package org.sparcs.soap.app.features.taxiPreview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.models.taxi.TaxiRoute
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.taxiPreview.components.TaxiDepartureHeader
import org.sparcs.soap.app.features.taxiPreview.components.TaxiRideStats
import org.sparcs.soap.app.features.taxiPreview.components.TaxiRiderRow
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.createKakaoMap
import org.sparcs.soap.app.shared.extensions.drawTaxiRoute
import org.sparcs.soap.app.shared.extensions.rememberTaxiRoomShare
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.shared.views.taxiRoomCell.components.TaxiRouteStops
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.downvote
import org.sparcs.soap.app.theme.ui.upvote
import org.sparcs.soap.buddyPreviewSupport.taxi.PreviewTaxiPreviewViewModel

private val MapMinHeight = 200.dp
private val FadeHeight = 48.dp

@Composable
fun TaxiPreviewView(
    room: TaxiRoom,
    viewModel: TaxiPreviewViewModelProtocol = hiltViewModel<TaxiPreviewViewModel>(),
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val share = rememberTaxiRoomShare(room)
    val blockStatus by viewModel.blockStatus.collectAsState()
    val taxiUser by viewModel.taxiUser.collectAsState()

    var route by remember(room.id) { mutableStateOf<TaxiRoute?>(null) }
    var isRouteMissing by remember(room.id) { mutableStateOf(false) }
    var detailsHeight by remember { mutableIntStateOf(0) }

    val isJoined = viewModel.isJoined(room.participants)
    val isFull = room.participants.size >= room.capacity
    val canJoin = taxiUser != null && (isJoined || (!isFull && !room.isDeparted && blockStatus == TaxiRoomBlockStatus.Allow))

    fun openChat(chatRoom: TaxiRoom) {
        onDismiss()
        val json = Uri.encode(Gson().toJson(chatRoom))
        navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
    }

    LaunchedEffect(room.id) {
        route = viewModel.calculateRoute(
            LatLng.from(room.source.latitude, room.source.longitude),
            LatLng.from(room.destination.latitude, room.destination.longitude)
        )
        isRouteMissing = route == null
    }

    Box(
        Modifier
            .fillMaxWidth()
            .analyticsScreen("Taxi Preview")
    ) {
        RouteMap(
            room = room,
            route = route,
            bottomInset = detailsHeight,
            modifier = Modifier.matchParentSize()
        )

        Column {
            Spacer(Modifier.height(MapMinHeight))

            PreviewDetails(
                room = room,
                route = route,
                showsStats = !isRouteMissing,
                joinLabel = joinLabel(room, isJoined, isFull, blockStatus),
                canJoin = canJoin,
                onShare = share,
                onJoin = {
                    scope.launch {
                        if (isJoined) openChat(room)
                        else viewModel.joinRoom(room.id)?.let(::openChat)
                    }
                },
                modifier = Modifier.onSizeChanged { detailsHeight = it.height }
            )
        }
    }

    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

@Composable
private fun PreviewDetails(
    room: TaxiRoom,
    route: TaxiRoute?,
    showsStats: Boolean,
    joinLabel: String,
    canJoin: Boolean,
    onShare: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetColor = MaterialTheme.colorScheme.surfaceContainerLow

    Column(modifier) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(FadeHeight)
                .background(Brush.verticalGradient(listOf(Color.Transparent, sheetColor)))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetColor)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            TaxiDepartureHeader(room.departAt)

            TaxiRouteStops(
                source = room.source.title.localized(),
                destination = room.destination.title.localized(),
                style = MaterialTheme.typography.titleLarge,
                markerSize = 16.dp,
                rowSpacing = 8.dp,
                maxLines = 2
            )

            if (showsStats) TaxiRideStats(room.departAt, route)

            TaxiRiderRow(room.participants, room.capacity)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalIconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
                }
                Button(
                    onClick = onJoin,
                    enabled = canJoin,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(joinLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun RouteMap(room: TaxiRoom, route: TaxiRoute?, bottomInset: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var isMapError by remember { mutableStateOf(false) }

    val pathColor = MaterialTheme.colorScheme.primary.toArgb()
    val sourceLabel = room.source.title.localized()
    val destinationLabel = room.destination.title.localized()

    if (LocalInspectionMode.current || isMapError) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest))
        return
    }

    LaunchedEffect(kakaoMap, route, bottomInset) {
        val map = kakaoMap ?: return@LaunchedEffect
        map.setPadding(0, 0, 0, bottomInset)
        map.drawTaxiRoute(
            context = context,
            startPos = LatLng.from(room.source.latitude, room.source.longitude),
            endPos = LatLng.from(room.destination.latitude, room.destination.longitude),
            pathPoints = route?.points.orEmpty(),
            pathColor = pathColor,
            startLabel = sourceLabel,
            endLabel = destinationLabel,
            startIconColor = downvote.toArgb(),
            endIconColor = upvote.toArgb()
        )
    }

    Box(modifier) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                ctx.createKakaoMap(
                    room,
                    onMapReady = { kakaoMap = it },
                    onError = { isMapError = true }
                )
            }
        )
        Box(
            Modifier
                .matchParentSize()
                .pointerInput(Unit) { detectTapGestures() }
        )
    }
}

@Composable
private fun joinLabel(room: TaxiRoom, isJoined: Boolean, isFull: Boolean, blockStatus: TaxiRoomBlockStatus): String =
    when {
        isJoined -> stringResource(R.string.joined_enter_chat)
        isFull -> stringResource(R.string.room_full)
        room.isDeparted -> stringResource(R.string.already_departed)
        blockStatus == TaxiRoomBlockStatus.TooManyRooms -> stringResource(R.string.room_limit_reached)
        blockStatus == TaxiRoomBlockStatus.NotPaid -> stringResource(R.string.room_settlement_required)
        else -> stringResource(R.string.join)
    }

@Preview
@Composable
private fun Preview() {
    Theme {
        TaxiPreviewView(
            room = TaxiRoom.mockList()[1],
            viewModel = PreviewTaxiPreviewViewModel(),
            onDismiss = {},
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun FullRoomPreview() {
    Theme {
        val room = TaxiRoom.mockList()[2]
        TaxiPreviewView(
            room = room.copy(capacity = room.participants.size),
            viewModel = PreviewTaxiPreviewViewModel(),
            onDismiss = {},
            navController = rememberNavController()
        )
    }
}
