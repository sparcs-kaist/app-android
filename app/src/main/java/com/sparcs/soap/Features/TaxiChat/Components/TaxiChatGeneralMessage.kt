package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sparcs.soap.Domain.Models.Taxi.TaxiChat
import com.sparcs.soap.R

@Composable
fun TaxiChatGeneralMessage(
    authorName: String?,
    type: TaxiChat.ChatType
) {
    val displayName = authorName ?: stringResource(R.string.unknown)
    when (type) {
        TaxiChat.ChatType.IN -> {
            Text(
                text = "$displayName has joined",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

        }
        TaxiChat.ChatType.OUT -> {
            Text(
                text = "$displayName has left",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        else -> {}
    }
}

@Preview
@Composable
private fun Preview() {
    Column {
        TaxiChatGeneralMessage("testuser", TaxiChat.ChatType.IN)
        TaxiChatGeneralMessage("testuser", TaxiChat.ChatType.OUT)
    }
}
