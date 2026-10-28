package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiChat.components.CallTaxiDialog
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date

/** The reminder before departure: the room's emoji to check when boarding, and a button to call a taxi. */
@Composable
fun ChatDepartureBubble(room: TaxiRoom, chatTime: Date) {
    var showDialog by remember { mutableStateOf(false) }

    val diffMinutes = (room.departAt.time - chatTime.time) / (60 * 1000)
    val alertText = when {
        diffMinutes <= 7 -> stringResource(R.string.taxi_5min_alert)
        diffMinutes <= 12 -> stringResource(R.string.taxi_10min_alert)
        else -> stringResource(R.string.taxi_15min_alert)
    }

    ChatCard {
        Text(text = alertText, style = MaterialTheme.typography.bodyLarge)

        // The room's emoji on the lighter surface, so it reads as a badge to compare.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.taxi_room_identifier_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(text = room.emojiIdentifier.display, fontSize = 48.sp)
                Text(
                    text = stringResource(R.string.taxi_check_room_id_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = { showDialog = true },
            // Rounded rectangles nest inside the card's corners.
            shape = MaterialTheme.shapes.medium,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.LocalTaxi, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.call_taxi))
        }
    }

    if (showDialog) {
        CallTaxiDialog(room = room, onDismiss = { showDialog = false })
    }
}

@Preview
@Composable
private fun Preview() {
    Theme { ChatDepartureBubble(room = TaxiRoom.mock(), chatTime = Date()) }
}
