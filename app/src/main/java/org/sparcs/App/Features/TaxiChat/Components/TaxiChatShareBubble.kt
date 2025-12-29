package org.sparcs.App.Features.TaxiChat.Components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.App.Shared.Extensions.formattedString
import org.sparcs.App.Shared.Mocks.mock
import org.sparcs.App.theme.ui.Theme
import org.sparcs.R
import java.util.Date

@Composable
fun TaxiChatShareBubble(
    room: TaxiRoom
) {
    val context = LocalContext.current
    val shareUrl = "${Constants.taxiInviteURL}${room.id}"
    val shareMessage = stringResource(
        R.string.taxi_share_message,
        room.departAt.formattedString(),
        room.source.title,
        room.destination.title,
        shareUrl
    )

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.share_now_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.share),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        TaxiChatUserWrapper(
            authorID = null,
            authorName = null,
            authorProfileImageURL = null,
            date = Date(),
            isMe = false,
            isGeneral = false,
            isWithdrawn = false,
            badge = true
        ) {
            TaxiChatShareBubble(room = TaxiRoom.mock())
        }
    }
}
