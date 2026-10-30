package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.shared.extensions.rememberTaxiRoomShare
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

/** The bot's prompt to invite others, with a share button. */
@Composable
fun ChatShareBubble(
    room: TaxiRoom,
) {
    val share = rememberTaxiRoomShare(room)

    ChatCard {
        Text(text = stringResource(R.string.share_now_prompt), style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = share,
            // Rounded rectangles nest inside the card's corners.
            shape = MaterialTheme.shapes.medium,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.share))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        ChatShareBubble(room = TaxiRoom.mock())
    }
}
