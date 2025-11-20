package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.ui.theme.Theme
import java.util.Date

@Composable
fun TaxiArrivalBubble() {
    Text(
        buildAnnotatedString {
            append("There are users who have not yet requested the settlement or have not completed the payment.\n\nPlease tap the ")
            append(buildBoldAnnotatedString("+ button"))
            append(" at the bottom left and press ")
            append(buildBoldAnnotatedString("Request Settlement"))
            append(" or ")
            append(buildBoldAnnotatedString("Send Payment"))
            append(" to complete the settlement request or payment.")
        },
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp)
    )
}


fun buildBoldAnnotatedString(vararg parts: String, normalText: String = "") = buildAnnotatedString {
    append(normalText)
    parts.forEach { part ->
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(part)
        pop()
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
            isWithdrawn = false
        ) {
            TaxiArrivalBubble()
        }
    }
}
