package org.sparcs.soap.app.features.post.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.summarization.PostSummaryPhase
import org.sparcs.soap.app.features.fullscreenImage.FullscreenImagesViewer
import org.sparcs.soap.app.features.post.PostBody
import org.sparcs.soap.app.shared.views.tiptap.TiptapImage
import org.sparcs.soap.app.shared.views.tiptap.TiptapImageView
import org.sparcs.soap.app.shared.views.tiptap.TiptapView
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun PostContent(
    body: PostBody?,
    contentKey: String,
    attachedImages: List<String>,
    isTranslating: Boolean,
    isHiddenForBlockedAuthor: Boolean,
    summaryPhase: PostSummaryPhase?,
    onRetrySummary: () -> Unit,
    onDismissSummary: () -> Unit,
    onLinkTapped: (String) -> Unit,
) {
    val galleryUrls = remember(body, attachedImages) {
        val inline = (body as? PostBody.Tiptap)?.layout?.images?.map { it.url }.orEmpty()
        inline + attachedImages.filterNot { it in inline }
    }
    var galleryStart by remember { mutableStateOf<Int?>(null) }
    val openImage = { url: String -> galleryStart = galleryUrls.indexOf(url).coerceAtLeast(0) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        PostSummary(phase = summaryPhase, onRetry = onRetrySummary, onDismiss = onDismissSummary)

        if (isHiddenForBlockedAuthor) {
            Text(
                text = stringResource(R.string.post_hidden_blocked_author),
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Crossfade(targetState = contentKey to body, label = "postBody") { (_, shown) ->
                TranslatingPlaceholder(isTranslating) {
                    PostBodyView(shown, onLinkTapped, openImage)
                }
            }
            attachedImages.forEach { url -> PostImage(url) { openImage(url) } }
        }
    }

    galleryStart?.let { start ->
        Dialog(
            onDismissRequest = { galleryStart = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            FullscreenImagesViewer(imageUrls = galleryUrls, startIndex = start, onDismiss = { galleryStart = null })
        }
    }
}

@Composable
private fun PostBodyView(body: PostBody?, onLinkTapped: (String) -> Unit, onImageClick: (String) -> Unit) {
    when (body) {
        is PostBody.Tiptap -> TiptapView(
            layout = body.layout,
            onLinkClick = onLinkTapped,
            image = { image -> PostImage(image.url) { onImageClick(image.url) } }
        )

        is PostBody.Html -> DynamicHeightWebView(
            html = body.html,
            baseUrl = body.baseUrl,
            onLinkTapped = onLinkTapped
        )

        null -> ContentSkeleton()
    }
}

@Composable
private fun TranslatingPlaceholder(isTranslating: Boolean, content: @Composable () -> Unit) {
    val pulse = if (isTranslating) {
        val transition = rememberInfiniteTransition(label = "translating")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = PULSE_ALPHA,
            animationSpec = infiniteRepeatable(tween(PULSE_DURATION_MS), RepeatMode.Reverse),
            label = "pulse",
        ).value
    } else 1f

    Box(
        Modifier
            .fillMaxWidth()
            .alpha(pulse)
            .then(if (isTranslating) Modifier.blockingTouches() else Modifier)
    ) {
        content()
    }
}

private fun Modifier.blockingTouches(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

@Composable
private fun PostImage(url: String, onClick: () -> Unit) {
    TiptapImageView(
        image = TiptapImage(url = url, alt = null, width = null),
        modifier = Modifier.clickable(role = Role.Image, onClickLabel = stringResource(R.string.open_image), onClick = onClick)
    )
}

private const val PULSE_ALPHA = 0.4f
private const val PULSE_DURATION_MS = 800

@Preview(showBackground = true)
@Composable
private fun PostContentPreview() {
    Theme {
        PostContent(
            body = PostBody.Html("<p>포탈 공지 <b>본문</b>입니다.</p>", "https://portal.kaist.ac.kr/"),
            contentKey = "original",
            attachedImages = emptyList(),
            isTranslating = true,
            isHiddenForBlockedAuthor = false,
            summaryPhase = PostSummaryPhase.Finished("A notice about the portal."),
            onRetrySummary = {},
            onDismissSummary = {},
            onLinkTapped = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostContentBlockedPreview() {
    Theme {
        PostContent(
            body = null,
            contentKey = "original",
            attachedImages = emptyList(),
            isTranslating = false,
            isHiddenForBlockedAuthor = true,
            summaryPhase = null,
            onRetrySummary = {},
            onDismissSummary = {},
            onLinkTapped = {},
        )
    }
}
