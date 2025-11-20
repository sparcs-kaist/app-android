package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Date

@Composable
fun TaxiChatPaymentBubble() {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                    topEnd = 24.dp
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "I sent the money!",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = MaterialTheme.colorScheme.background
        )
    }
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
        TaxiChatPaymentBubble()
    }
}
