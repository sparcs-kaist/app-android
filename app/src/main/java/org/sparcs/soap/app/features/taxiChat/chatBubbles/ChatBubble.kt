package org.sparcs.soap.app.features.taxiChat.chatBubbles

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.enums.DeepLinkEventBus
import org.sparcs.soap.app.domain.models.taxi.ChatBubblePosition
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.shared.extensions.toDetectedAnnotatedString
import org.sparcs.soap.app.shared.mocks.taxi.mockList
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The corners of a chat bubble: round, except the corner nearest the sender on the last bubble of
 * a group.
 */
internal fun chatBubbleShape(isMine: Boolean, position: ChatBubblePosition): RoundedCornerShape {
    val isLast = position == ChatBubblePosition.BOTTOM || position == ChatBubblePosition.SINGLE
    return RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (!isMine && isLast) 4.dp else 20.dp,
        bottomEnd = if (isMine && isLast) 4.dp else 20.dp
    )
}

/**
 * A text message: primary for yours, the card grey for others'. Long press selects text, so part
 * of a message can be copied.
 */
@Composable
fun ChatBubble(
    chat: TaxiChat,
    position: ChatBubblePosition,
    isMine: Boolean,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val backgroundColor =
        if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor =
        if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val urlColor =
        if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    val formattedContent = remember(chat.content, urlColor) {
        chat.content.toDetectedAnnotatedString(urlColor)
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Surface(
        shape = chatBubbleShape(isMine, position),
        color = backgroundColor,
        contentColor = contentColor,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        SelectionContainer {
            Text(
                text = formattedContent,
                style = MaterialTheme.typography.bodyLarge,
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
                            }
                        )
                    },
                onTextLayout = { textLayoutResult = it }
            )
        }
    }
}

/** An empty bubble of a random width, for the loading state. */
@Composable
fun ChatBubbleSkeleton() {
    val width = remember { (120..240).random().dp }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .size(width = width, height = 44.dp)
    ) {}
}

private fun handleURL(
    context: Context,
    urlString: String,
    scope: CoroutineScope,
) {
    val uri = if (!urlString.startsWith("http")) "http://$urlString" else urlString.toUri()
    val deepLink = DeepLink.fromUri(uri as Uri?)

    if (deepLink != null) {
        scope.launch {
            DeepLinkEventBus.post(deepLink)
        }
    } else {
        try {
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(context, uri)
        } catch (_: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}

@Preview(name = "Mine - Single Bubble", showBackground = true)
@Composable
private fun MineSingleBubblePreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubble(
                chat = TaxiChat.mockList()[0].copy(content = "내가 보낸 단일 메시지입니다."),
                position = ChatBubblePosition.SINGLE,
                isMine = true
            )
        }
    }
}

@Preview(name = "Others - Single Bubble", showBackground = true)
@Composable
private fun OthersSingleBubblePreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubble(
                chat = TaxiChat.mockList()[0].copy(content = "상대방이 보낸 단일 메시지입니다."),
                position = ChatBubblePosition.SINGLE,
                isMine = false
            )
        }
    }
}

@Preview(name = "Mine - Message Group (Top)", showBackground = true)
@Composable
private fun MineGroupTopPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubble(
                chat = TaxiChat.mockList()[0].copy(content = "연속된 메시지의 시작입니다."),
                position = ChatBubblePosition.TOP,
                isMine = true
            )
        }
    }
}

@Preview(name = "Others - Message Group (Bottom)", showBackground = true)
@Composable
private fun OthersGroupBottomPreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubble(
                chat = TaxiChat.mockList()[0].copy(content = "연속된 메시지의 마지막입니다."),
                position = ChatBubblePosition.BOTTOM,
                isMine = false
            )
        }
    }
}

@Preview(name = "Link Interaction Bubble", showBackground = true)
@Composable
private fun LinkBubblePreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubble(
                chat = TaxiChat.mockList()[0].copy(content = "링크 포함 메시지: https://www.kaist.ac.kr"),
                position = ChatBubblePosition.SINGLE,
                isMine = false
            )
        }
    }
}

@Preview(name = "Skeleton Bubble", showBackground = true)
@Composable
private fun SkeletonBubblePreview() {
    Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            ChatBubbleSkeleton()
        }
    }
}
