package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiChat

/** "Bob has joined" or "Bob has left", as a quiet centred line. */
@Composable
fun ChatGeneralMessage(
    authorName: String?,
    type: TaxiChat.ChatType,
) {
    val displayName = authorName ?: stringResource(R.string.unknown)
    val text = when (type) {
        TaxiChat.ChatType.ENTRANCE -> stringResource(R.string.has_joined, displayName)
        TaxiChat.ChatType.EXIT -> stringResource(R.string.has_left, displayName)
        else -> return
    }
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Preview
@Composable
private fun Preview() {
    Column {
        ChatGeneralMessage("testuser", TaxiChat.ChatType.ENTRANCE)
        ChatGeneralMessage("testuser", TaxiChat.ChatType.EXIT)
    }
}
