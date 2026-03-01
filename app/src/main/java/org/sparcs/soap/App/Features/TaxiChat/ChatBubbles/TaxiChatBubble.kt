package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Enums.DeepLinkEventBus

@Composable
fun TaxiChatBubble(
    content: String,
    showTip: Boolean,
    isMe: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val backgroundColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val urlColor = if (isMe) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val annotatedString = buildAnnotatedString {
        val regex = Patterns.WEB_URL.toRegex()
        var lastIndex = 0
        regex.findAll(content).forEach { matchResult ->
            val range = matchResult.range
            append(content.substring(lastIndex, range.first))
            val url = content.substring(range)
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(color = urlColor, textDecoration = TextDecoration.Underline)
            ) {
                append(url)
            }
            pop()
            lastIndex = range.last + 1
        }
        if (lastIndex < content.length) append(content.substring(lastIndex))
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
        SelectionContainer {
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier
                    .padding(12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                textLayoutResult?.let { layoutResult ->
                                    val position = layoutResult.getOffsetForPosition(offset)
                                    annotatedString.getStringAnnotations("URL", position, position)
                                        .firstOrNull()?.let { annotation ->
                                            handleURL(context, annotation.item, scope)
                                        }
                                }
                            },
                            onLongPress = {
                                clipboardManager.setText(AnnotatedString(content))
                            }
                        )
                    },
                onTextLayout = { textLayoutResult = it }
            )
        }
    }
}

private fun handleURL(
    context: Context,
    urlString: String,
    scope: CoroutineScope
) {
    val uri = Uri.parse(if (!urlString.startsWith("http")) "http://$urlString" else urlString)
    val deepLink = DeepLink.fromUri(uri)

    if (deepLink != null) {
        scope.launch {
            DeepLinkEventBus.post(deepLink)
        }
    } else {
        try {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, uri)
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
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
