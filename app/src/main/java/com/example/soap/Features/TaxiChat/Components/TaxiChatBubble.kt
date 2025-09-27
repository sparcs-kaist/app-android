package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Features.SafariViewWrapper.SafariViewWrapper

@Composable
fun TaxiChatBubble(
    content: String,
    showTip: Boolean,
    isMe: Boolean
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var selectedUrl by remember { mutableStateOf<String?>(null) }

    val backgroundColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val urlColor = if (isMe) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        val regex = "(https?://[\\w./?=&-]+)".toRegex()
        var lastIndex = 0
        regex.findAll(content).forEach { matchResult ->
            val range = matchResult.range
            append(content.substring(lastIndex, range.first))
            val url = content.substring(range)
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(
                    color = urlColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(url)
            }
            pop()
            lastIndex = range.last + 1
        }
        if (lastIndex < content.length) {
            append(content.substring(lastIndex))
        }
    }

    val bubbleShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = if (!isMe && showTip) 4.dp else 24.dp,
        bottomEnd = if (isMe && showTip) 4.dp else 24.dp,
    )

    Surface(
        shape = bubbleShape,
        color = backgroundColor
    ) {
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier
                .padding(12.dp)
                .combinedClickable(
                    onClick = {
                        annotatedString.getStringAnnotations(tag = "URL", start = 0, end = annotatedString.length)
                            .firstOrNull()?.let { stringAnnotation ->
                                selectedUrl = stringAnnotation.item
                            }
                    },
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString(content))
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }

    selectedUrl?.let { url ->
        SafariViewWrapper(url) { selectedUrl = null }
    }
}

@Preview
@Composable
private fun Preview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        TaxiChatBubble(content = "Visit https://naver.com now!", showTip = true, isMe = true)
        TaxiChatBubble(content = "Here is a link: https://apple.com and some more text.", showTip = true, isMe = false)
        TaxiChatBubble(content = "No link here just chatting casually", showTip = false, isMe = true)
        TaxiChatBubble(
            content = "Multiple links: https://swift.org and also https://developer.apple.com",
            showTip = true,
            isMe = false
        )
    }
}
