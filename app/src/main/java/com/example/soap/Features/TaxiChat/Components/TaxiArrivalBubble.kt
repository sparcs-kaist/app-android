package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.SoapTheme
import java.util.Date

@Composable
fun TaxiArrivalBubble() {
    Text(
        text = "There are users who have not yet requested the settlement or have not completed the payment.\n\n" +
                "Please tap the + button at the bottom left and press Request Settlement or Send Payment to complete the settlement request or payment.",
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}


@Preview
@Composable
private fun Preview() {
    SoapTheme {
        TaxiChatUserWrapper(
            authorID = null,
            authorName = null,
            authorProfileImageURL = null,
            date = Date(),
            isMe = false,
            isGeneral = false,
            isWithdrawn = false
        ) {
            TaxiArrivalBubble()
        }
    }
}
