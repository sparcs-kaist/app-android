package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiDeepLinkHelper
import org.sparcs.soap.App.Shared.Extensions.openUri
import org.sparcs.soap.R

@Composable
fun ChatDepartureBubble(room: TaxiRoom) {
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
            text = stringResource(R.string.taxi_15min_alert)
        )

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Directions,
                contentDescription = "Call Taxi"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.call_taxi), style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.call_taxi)) },
            text = {
                Text(
                    stringResource(
                        R.string.taxi_launch_info,
                        room.source.title,
                        room.destination.title
                    )
                )
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        context.openUri(
                            uri = TaxiDeepLinkHelper.getKakaoTUri(room.source, room.destination),
                            packageName = "com.kakao.taxi"
                        )
                        showDialog = false
                    }) {
                        Text(stringResource(R.string.open_kakao_t))
                    }

                    TextButton(onClick = {
                        context.openUri(
                            uri = TaxiDeepLinkHelper.getUberUri(room.source, room.destination),
                            packageName = "com.ubercab"
                        )
                        showDialog = false
                    }) {
                        Text(stringResource(R.string.open_uber))
                    }
                }
            },
        )
    }
}
//
//@Preview
//@Composable
//private fun Preview() {
//    TaxiChatUserWrapper(
//        authorID = null,
//        authorName = null,
//        authorProfileImageURL = null,
//        date = Date(),
//        isMe = false,
//        isGeneral = false,
//        isWithdrawn = false,
//        badge = true
//    ) {
//        ChatDepartureBubble(room = TaxiRoom.mock())
//    }
//}
