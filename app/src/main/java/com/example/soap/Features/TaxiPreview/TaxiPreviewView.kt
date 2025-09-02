package com.example.soap.Features.TaxiPreview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.TaxiPreview.Components.InfoRow
import com.example.soap.Features.TaxiPreview.Components.RouteHeaderView
import com.example.soap.Shared.Extensions.formattedString
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.Views.TaxiRoomCell.Components.TaxiParticipantsIndicator
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch


@Composable
fun TaxiPreviewView(
    room: TaxiRoom,
    viewModel: TaxiPreviewViewModel = hiltViewModel(),
    onDismiss: ()-> Unit
) {
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(room.source.latitude, room.source.longitude), 13f)
    }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var pathPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            pathPoints = viewModel.calculateRoutePoints(
                LatLng(room.source.latitude, room.source.longitude),
                LatLng(room.destination.latitude, room.destination.longitude)
            )
        } catch (e: Exception) {
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
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = LatLng(room.source.latitude, room.source.longitude)),
                title = room.source.title.localized(),
                snippet = "Source"
            )
            Marker(
                state = MarkerState(position = LatLng(room.source.latitude, room.source.longitude)),
                title = room.destination.title.localized(),
                snippet = "Destination"
            )
            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color.Blue)
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically){

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
                IconButton(onClick = {/*Todo-share*/}) {
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
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Unknown error"
                                showError = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = room.participants.size < room.capacity &&
                            !viewModel.isJoined(room.participants)
                ) {
                    Text(
                        when {
                            viewModel.isJoined(room.participants) -> "Joined"
                            room.participants.size >= room.capacity -> "This room is full"
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
        TaxiPreviewView(room = TaxiRoom.mockList()[1], onDismiss = {})
    }
}
