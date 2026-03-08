package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.DeepLink
import org.sparcs.soap.App.Domain.Enums.DeepLinkEventBus
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatBubblePosition
import org.sparcs.soap.App.Shared.Extensions.toDetectedAnnotatedString

@Composable
fun ChatBubble(
    chat: TaxiChat,
    position: ChatBubblePosition,
    isMine: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val backgroundColor = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val urlColor = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary

    val bubbleShape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = if (!isMine && (position == ChatBubblePosition.BOTTOM || position == ChatBubblePosition.SINGLE)) 4.dp else 24.dp,
        bottomEnd = if (isMine && (position == ChatBubblePosition.BOTTOM || position == ChatBubblePosition.SINGLE)) 4.dp else 24.dp
    )

    val formattedContent = remember(chat.content) {
        chat.content.toDetectedAnnotatedString(urlColor)
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Surface(
        shape = bubbleShape,
        color = backgroundColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        SelectionContainer {
            Text(
                text = formattedContent,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                textLayoutResult?.let { layoutResult ->
                                    val pos = layoutResult.getOffsetForPosition(offset)
                                    formattedContent.getStringAnnotations("URL", pos, pos)
                                        .firstOrNull()?.let { annotation ->
                                            handleURL(context, annotation.item, scope)
                                        }
                                }
                            },
                            onLongPress = {
                                clipboardManager.setText(AnnotatedString(chat.content))
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