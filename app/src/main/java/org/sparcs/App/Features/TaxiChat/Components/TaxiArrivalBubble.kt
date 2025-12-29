package org.sparcs.App.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.R
import java.util.Date

@Composable
fun TaxiArrivalBubble() {
    val bubbleText = stringResource(
        id = R.string.taxi_arrival_bubble,
        "\n\n",
        buildBoldAnnotatedString("+"+ stringResource(R.string.button)),
        buildBoldAnnotatedString(stringResource(R.string.request_settlement)),
        buildBoldAnnotatedString(stringResource(R.string.send_payment))
    )

    Text(
        text = bubbleText,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp),
        color = MaterialTheme.colorScheme.onSurface
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
            isWithdrawn = false,
            badge = true
        ) {
            TaxiArrivalBubble()
        }
    }
}
