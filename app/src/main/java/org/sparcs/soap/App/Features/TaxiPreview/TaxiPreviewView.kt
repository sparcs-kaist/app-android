package org.sparcs.soap.App.Features.TaxiPreview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.sparcs.soap.App.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.TaxiPreview.Components.InfoRow
import org.sparcs.soap.App.Features.TaxiPreview.Components.RouteHeaderView
import org.sparcs.soap.App.Features.TaxiPreview.Components.TaxiCarrierIndicator
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Extensions.formattedString
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Taxi.MockTaxiPreviewViewModel
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.Components.TaxiParticipantsIndicator
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.grayBB
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
    var pathPoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    val blockStatus = viewModel.blockStatus.collectAsState().value
    val routeColor = MaterialTheme.colorScheme.primary.toArgb()

    val isJoinButtonDisabled: Boolean =
        !viewModel.isJoined(room.participants) && (room.participants.size >= room.capacity ||
                room.isDeparted ||
                blockStatus != TaxiRoomBlockStatus.Allow)

    val mapView = remember {
        if (!isPreview) {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                isClickable = false
                setBuiltInZoomControls(false)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(room.source.latitude, room.source.longitude))
            }
        } else null
    }

    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )

    LaunchedEffect(Unit) {
        try {
            pathPoints = viewModel.calculateRoutePoints(
                source = GeoPoint(room.source.latitude, room.source.longitude),
                destination = GeoPoint(room.destination.latitude, room.destination.longitude)
            )

        } catch (e: Exception) {
            Timber.e("Error calculating route points: ${e.message}")
            errorMessage = e.localizedMessage ?: "Unknown error"
            showError = true
        }
    }

    DisposableEffect(mapView) {
        mapView?.onResume()
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .matchParentSize(),
                    factory = { mapView!! },
                    update = { mapView ->
                        mapView.overlays.clear()

                        val startMarker = Marker(mapView).apply {
                            position = GeoPoint(room.source.latitude, room.source.longitude)
                            title = room.source.title.localized()
                            icon =
                                ContextCompat.getDrawable(
                                    mapView.context,
                                    R.drawable.round_location_on
                                )
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }

                        val endMarker = Marker(mapView).apply {
                            position =
                                GeoPoint(room.destination.latitude, room.destination.longitude)
                            title = room.destination.title.localized()
                            icon =
                                ContextCompat.getDrawable(mapView.context, R.drawable.arrival_point)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }

                        mapView.overlays.add(startMarker)
                        mapView.overlays.add(endMarker)

                        if (pathPoints.isNotEmpty()) {
                            val bounds = BoundingBox.fromGeoPoints(pathPoints)

                            val polyline = Polyline().apply {
                                setPoints(pathPoints)
                                outlinePaint.color = routeColor
                                outlinePaint.strokeWidth = 8f
                            }
                            mapView.overlays.add(polyline)

                            val padding = 50
                            mapView.zoomToBoundingBox(bounds, true, padding)
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
            viewModel = MockTaxiPreviewViewModel(),
            onDismiss = {},
            navController = rememberNavController()
        )
    }
}