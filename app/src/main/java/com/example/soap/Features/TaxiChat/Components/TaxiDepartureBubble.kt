package com.example.soap.Features.TaxiChat.Components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import java.util.Date

@Composable
fun TaxiDepartureBubble(room: TaxiRoom) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⏰ It's 15 minutes before your taxi leaves! If everyone's gathered, go ahead and call the taxi to head out together.",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.round_directions),
                contentDescription = "Call Taxi"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Call Taxi", style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Call Taxi") },
            text = {
                Text(
                    "You can launch the taxi app with the departure and destination already set. Once everyone has gathered at the departure point, press the button to call a taxi from ${room.source.title} to ${room.destination.title}."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    openKakaoT(context, room)
                    showDialog = false
                }) {
                    Text("Open Kakao T")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        openUber(context, room)
                        showDialog = false
                    }) {
                        Text("Open Uber")
                    }
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

private fun openKakaoT(context: android.content.Context, room: TaxiRoom) {
    val uri = Uri.parse(
        "kakaot://taxi/set?" +
                "dest_lng=${room.destination.longitude}&dest_lat=${room.destination.latitude}" +
                "&origin_lng=${room.source.longitude}&origin_lat=${room.source.latitude}"
    )
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

private fun openUber(context: android.content.Context, room: TaxiRoom) {
    val uri = Uri.parse(
        "uber://?action=setPickup&client_id=a" +
                "&pickup[latitude]=${room.source.latitude}&pickup[longitude]=${room.source.longitude}" +
                "&dropoff[latitude]=${room.destination.latitude}&dropoff[longitude]=${room.destination.longitude}"
    )
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
}

@Preview
@Composable
private fun Preview() {
    TaxiChatUserWrapper(
        authorID = null,
        authorName = null,
        authorProfileImageURL = null,
        date = Date(),
        isMe = false,
        isGeneral = false,
        isWithdrawn = false
    ) {
        TaxiDepartureBubble(room = TaxiRoom.mock())
    }
}
