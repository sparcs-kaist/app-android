package com.example.soap.Features.TaxiPreview

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Enums.TaxiRoomBlockStatus
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.TaxiPreview.Components.InfoRow
import com.example.soap.Features.TaxiPreview.Components.RouteHeaderView
import com.example.soap.R
import com.example.soap.Shared.Extensions.formattedString
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.Views.TaxiRoomCell.Components.TaxiParticipantsIndicator
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun TaxiPreviewView(
    room: TaxiRoom,
    viewModel: TaxiPreviewViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val blockStatus = viewModel.blockStatus.collectAsState().value
    val routeColor = MaterialTheme.colorScheme.primary.toArgb()

    val isJoinButtonDisabled: Boolean =
        !viewModel.isJoined(room.participants) && (room.participants.size >= room.capacity ||
                room.isDeparted ||
                blockStatus != TaxiRoomBlockStatus.Allow)

    LaunchedEffect(Unit) {
        try {
            pathPoints = viewModel.calculateRoutePoints(
                source = LatLng(room.source.latitude, room.source.longitude),
                destination = LatLng(room.destination.latitude, room.destination.longitude)
            )

        } catch (e: Exception) {
            Log.e("TaxiPreviewView", "Error calculating route points: ${e.message}")
            errorMessage = e.localizedMessage ?: "Unknown error"
            showError = true
        }
    }


    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("Okay")
                }
            },
            title = { Text("Error") },
            text = { Text(errorMessage) }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clipToBounds()
        ) {
            //MAP
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize(),
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(false)
                        isClickable = false
                        setBuiltInZoomControls(false)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(room.source.latitude, room.source.longitude))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    val startMarker = Marker(mapView).apply {
                        position = GeoPoint(room.source.latitude, room.source.longitude)
                        title = room.source.title.localized()
                        icon =
                            ContextCompat.getDrawable(mapView.context, R.drawable.round_location_on)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    val endMarker = Marker(mapView).apply {
                        position = GeoPoint(room.destination.latitude, room.destination.longitude)
                        title = room.destination.title.localized()
                        icon = ContextCompat.getDrawable(mapView.context, R.drawable.arrival_point)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    mapView.overlays.add(startMarker)
                    mapView.overlays.add(endMarker)

                    if (pathPoints.isNotEmpty()) {
                        val geoPoints = listOf(
                            GeoPoint(room.source.latitude, room.source.longitude),
                            GeoPoint(room.destination.latitude, room.destination.longitude)
                        )
                        val bounds = BoundingBox.fromGeoPoints(geoPoints)

                        val polyline = Polyline().apply {
                            setPoints(pathPoints.map { GeoPoint(it.latitude, it.longitude) })
                            outlinePaint.color = routeColor
                            outlinePaint.strokeWidth = 8f
                        }
                        mapView.overlays.add(polyline)

                        val padding = 50
                        mapView.zoomToBoundingBox(bounds, true, padding)
                    }
                }
            )
        }
        //______________________________________________________

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
            }

            RouteHeaderView(
                source = room.source.title.localized(),
                destination = room.destination.title.localized()
            )

            Spacer(Modifier.padding(8.dp))

            InfoRow(label = "Depart at", value = room.departAt.formattedString())

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = {
                    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
                    val shareMessage =
                        "🚕 Looking for someone to ride with on ${room.departAt.formattedString()} from ${room.source.title} to ${room.destination.title}! 🚕\n$shareUrl"

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
                                viewModel.joinRoom(
                                    id = room.id
                                )
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
                            viewModel.isJoined(room.participants) -> "Joined(Enter chat)"
                            room.participants.size >= room.capacity -> "This room is full"
                            room.isDeparted -> "Already Departed"
                            blockStatus == TaxiRoomBlockStatus.TooManyRooms -> "Room Limit Reached"
                            blockStatus == TaxiRoomBlockStatus.NotPaid -> "Room Settlement Required"
                            else -> "Join"
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        TaxiPreviewView(
            room = TaxiRoom.mockList()[1],
            onDismiss = {},
            navController = rememberNavController()
        )
    }
}