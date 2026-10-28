package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.R
import org.sparcs.soap.app.theme.ui.Theme

/** The bot's reminder after arrival to request or send the settlement. */
@Composable
fun ChatArrivalBubble() {
    val bubbleText = stringResource(
        id = R.string.taxi_arrival_bubble,
        "\n\n",
        buildBoldAnnotatedString("+" + stringResource(R.string.button)),
        buildBoldAnnotatedString(stringResource(R.string.request_settlement)),
        buildBoldAnnotatedString(stringResource(R.string.send_payment))
    )

    ChatCard {
        Text(text = bubbleText, style = MaterialTheme.typography.bodyLarge)
    }
}


fun buildBoldAnnotatedString(vararg parts: String, normalText: String = "") = buildAnnotatedString {
    append(normalText)
    parts.forEach { part ->
        pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold))
        append(part)
        pop()
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        ChatArrivalBubble()
    }
}
