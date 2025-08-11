package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TaxiChatGeneralMessage(
    authorName: String?,
    type: ChatType
) {
    val displayName = authorName ?: "unknown"
    when (type) {
        ChatType.Entrance -> {
            Text(
                text = "$displayName has joined",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        ChatType.Exit -> {
            Text(
                text = "$displayName has left",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        else -> {}
    }
}

enum class ChatType {
    Entrance, Exit, Other
}

@Preview
@Composable
private fun Preview() {
    Column {
        TaxiChatGeneralMessage("testuser", ChatType.Entrance)
        TaxiChatGeneralMessage("testuser", ChatType.Exit)
    }
}
