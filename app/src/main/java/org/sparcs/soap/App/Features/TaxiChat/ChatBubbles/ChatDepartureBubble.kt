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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.App.Domain.Helpers.TaxiDeepLinkHelper
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Shared.Extensions.openUri
import org.sparcs.soap.App.Shared.Mocks.Taxi.mock
import org.sparcs.soap.App.theme.ui.Theme
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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.taxi_room_identifier_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = room.emojiIdentifier.display,
                    fontSize = 48.sp
                )

                Text(
                    text = stringResource(R.string.taxi_check_room_id_notice),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

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

@Preview
@Composable
private fun Preview() {
    Theme { ChatDepartureBubble(room = TaxiRoom.mock()) }
}
