package org.sparcs.soap.App.Features.TaxiPreview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiPreview.Components.InfoRow
import org.sparcs.soap.App.Features.TaxiPreview.Components.RouteHeaderView
import org.sparcs.soap.App.Features.TaxiPreview.Components.TaxiCarrierIndicator
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.drawTaxiRoute
import org.sparcs.soap.App.Shared.Extensions.formattedString
import org.sparcs.soap.App.Shared.Mocks.Taxi.mockList
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.Components.TaxiParticipantsIndicator
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.downvote
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.App.theme.ui.upvote
import org.sparcs.soap.BuddyPreviewSupport.Taxi.PreviewTaxiPreviewViewModel
import org.sparcs.soap.R
import timber.log.Timber

@Composable
fun TaxiPreviewView(
    room: TaxiRoom,
    viewModel: TaxiPreviewViewModelProtocol = hiltViewModel(),
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isPreview = LocalInspectionMode.current
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isMapLoading by remember { mutableStateOf(true) }

    val blockStatus = viewModel.blockStatus.collectAsState().value
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val pathColor = MaterialTheme.colorScheme.primary.toArgb()
    val sourceString = stringResource(R.string.source)
    val destinationString = stringResource(R.string.destination)

    val isJoinButtonDisabled: Boolean =
        !viewModel.isJoined(room.participants) && (room.participants.size >= room.capacity ||
                room.isDeparted ||
                blockStatus != TaxiRoomBlockStatus.Allow)

    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )
    LaunchedEffect(room) {
        pathPoints = viewModel.calculateRoutePoints(
            LatLng.from(room.source.latitude, room.source.longitude),
            LatLng.from(room.destination.latitude, room.destination.longitude)
        )
    }

    LaunchedEffect(kakaoMap, pathPoints) {
        val map = kakaoMap ?: return@LaunchedEffect
        if (pathPoints.isEmpty()) return@LaunchedEffect

        map.drawTaxiRoute(
            context = context,
            startPos = LatLng.from(room.source.latitude, room.source.longitude),
            endPos = LatLng.from(room.destination.latitude, room.destination.longitude),
            pathPoints = pathPoints,
            pathColor = pathColor,
            startLabel = sourceString,
            endLabel = destinationString,
            startIconColor = downvote.toArgb(),
            endIconColor = upvote.toArgb()
        )
        isMapLoading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .analyticsScreen("Taxi Preview")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clipToBounds()
        ) {
            //MAP
            if (!isPreview) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            start(object : MapLifeCycleCallback() {
                                override fun onMapDestroy() {}
                                override fun onMapError(e: Exception?) { Timber.e(e) }
                            }, object : KakaoMapReadyCallback() {
                                override fun onMapReady(map: KakaoMap) {
                                    kakaoMap = map
                                    map.moveCamera(
                                        CameraUpdateFactory.fitMapPoints(
                                            arrayOf(LatLng.from(room.source.latitude, room.source.longitude), LatLng.from(room.destination.latitude, room.destination.longitude)),
                                            100
                                        )
                                    )
                                }
                            })
                        }
                    }
                )
                if (isMapLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = {}, onTap = {}, onPress = {})
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Map Preview")
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = room.title,
                    color = MaterialTheme.colorScheme.grayBB
                )

                Spacer(Modifier.weight(1f))

                TaxiParticipantsIndicator(
                    participants = room.participants.size,
                    capacity = room.capacity
                )

                TaxiCarrierIndicator(
                    carrierCount = room.participants.count { it.hasCarrier }
                )
            }

            RouteHeaderView(
                source = room.source.title.localized(),
                destination = room.destination.title.localized()
            )

            Spacer(Modifier.padding(8.dp))

            InfoRow(
                label = stringResource(R.string.depart_at),
                value = room.departAt.formattedString()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareMessage)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.joinRoom(id = room.id)
                                onDismiss()
                                val json = Uri.encode(Gson().toJson(room))
                                navController.navigate(Channel.TaxiChatView.name + "?room_json=$json")
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                showError = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isJoinButtonDisabled
                ) {
                    Text(
                        when {
                            viewModel.isJoined(room.participants) -> stringResource(R.string.joined_enter_chat)
                            room.participants.size >= room.capacity -> stringResource(R.string.room_full)
                            room.isDeparted -> stringResource(R.string.already_departed)
                            blockStatus == TaxiRoomBlockStatus.TooManyRooms -> stringResource(R.string.room_limit_reached)
                            blockStatus == TaxiRoomBlockStatus.NotPaid -> stringResource(R.string.room_settlement_required)
                            else -> stringResource(R.string.join)
                        }
                    )
                }
            }
        }
    }
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage) }
        )
    }
}

//______________________________________________________

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