package org.sparcs.soap.app.shared.views.tiptap

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import org.sparcs.soap.app.domain.helpers.tiptap.TiptapParser
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun TiptapView(
    layout: TiptapLayout,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable (TiptapImage) -> Unit = { TiptapImageView(it) },
) {
    SelectionContainer(modifier) {
        TiptapBlockStack(layout.blocks, BLOCK_SPACING, onLinkClick, image)
    }
}

@Composable
private fun TiptapBlockStack(
    blocks: List<TiptapBlock>,
    spacing: Int,
    onLinkClick: (String) -> Unit,
    image: @Composable (TiptapImage) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        blocks.forEach { block -> TiptapBlockView(block, onLinkClick, image) }
    }
}

@Composable
private fun TiptapBlockView(
    block: TiptapBlock,
    onLinkClick: (String) -> Unit,
    image: @Composable (TiptapImage) -> Unit,
) {
    when (val kind = block.kind) {
        is TiptapBlock.Kind.Paragraph -> TiptapText(
            text = kind.text.ifEmpty { AnnotatedString(" ") },
            style = MaterialTheme.typography.bodyLarge,
            onLinkClick = onLinkClick
        )

        is TiptapBlock.Kind.Heading -> TiptapText(
            text = kind.text,
            style = when (kind.level) {
                1 -> MaterialTheme.typography.titleLarge
                2 -> MaterialTheme.typography.titleMedium
                else -> MaterialTheme.typography.titleSmall
            },
            onLinkClick = onLinkClick,
            modifier = Modifier.semantics { heading() }
        )

        is TiptapBlock.Kind.CodeBlock -> TiptapCodeBlock(kind.code)
        is TiptapBlock.Kind.Image -> image(kind.image)
        is TiptapBlock.Kind.Bookmark -> TiptapBookmarkCard(kind.bookmark, onLinkClick)
        is TiptapBlock.Kind.Blockquote -> TiptapBlockquote(kind.blocks, onLinkClick, image)
        is TiptapBlock.Kind.ListBlock -> TiptapListView(kind.list, onLinkClick, image)
        TiptapBlock.Kind.HorizontalRule -> HorizontalDivider(Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun TiptapText(
    text: AnnotatedString,
    style: TextStyle,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkStyles = TextLinkStyles(
        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
    )
    val linked = remember(text, linkStyles) {
        text.mapAnnotations { range ->
            val link = range.item as? LinkAnnotation.Url ?: return@mapAnnotations range
            AnnotatedString.Range(
                LinkAnnotation.Url(link.url, linkStyles) { onLinkClick(link.url) },
                range.start,
                range.end
            )
        }
    }
    Text(text = linked, style = style, modifier = modifier)
}

@Composable
private fun TiptapCodeBlock(code: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(code, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace, softWrap = false)
    }
}

@Composable
private fun TiptapBlockquote(
    blocks: List<TiptapBlock>,
    onLinkClick: (String) -> Unit,
    image: @Composable (TiptapImage) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(IntrinsicSize.Min)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            TiptapBlockStack(blocks, NESTED_SPACING, onLinkClick, image)
        }
    }
}

@Composable
private fun TiptapListView(
    list: TiptapList,
    onLinkClick: (String) -> Unit,
    image: @Composable (TiptapImage) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(NESTED_SPACING.dp)) {
        list.items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.marker,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.widthIn(min = 16.dp)
                )
                Box(Modifier.weight(1f)) {
                    TiptapBlockStack(item.blocks, NESTED_SPACING, onLinkClick, image)
                }
            }
        }
    }
}

@Composable
fun TiptapImageView(image: TiptapImage, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = image.url,
        contentDescription = image.alt,
        contentScale = ContentScale.FillWidth,
        loading = { TiptapImagePlaceholder(failed = false) },
        error = { TiptapImagePlaceholder(failed = true) },
        modifier = modifier
            .then(if (image.width != null) Modifier.widthIn(max = image.width.dp) else Modifier)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
    )
}

@Composable
private fun TiptapImagePlaceholder(failed: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (failed) {
            Icon(Icons.Outlined.BrokenImage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun TiptapBookmarkCard(bookmark: TiptapBookmark, onLinkClick: (String) -> Unit) {
    OutlinedCard(onClick = { onLinkClick(bookmark.url) }, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(bookmark.displayTitle) },
            supportingContent = {
                Text(bookmark.url, maxLines = 1, overflow = TextOverflow.MiddleEllipsis)
            },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.semantics { contentDescription = bookmark.displayTitle }
        )
    }
}

private const val BLOCK_SPACING = 12
private const val NESTED_SPACING = 6

private val previewLayout = TiptapLayout.from(
    TiptapParser().parse(
        """
        {"type":"doc","content":[
          {"type":"heading","attrs":{"level":2},"content":[{"type":"text","text":"동아리 신입 부원 모집"}]},
          {"type":"paragraph","content":[
            {"type":"text","text":"지원은 "},
            {"type":"text","marks":[{"type":"bold"}],"text":"금요일까지"},
            {"type":"text","text":" "},
            {"type":"text","marks":[{"type":"link","attrs":{"href":"https://sparcs.org"}}],"text":"이 링크"},
            {"type":"text","text":"에서 받습니다."}
          ]},
          {"type":"bulletList","content":[
            {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"매주 목요일 모임"}]}]},
            {"type":"listItem","content":[{"type":"paragraph","content":[{"type":"text","text":"신입 교육 제공"}]}]}
          ]},
          {"type":"blockquote","content":[{"type":"paragraph","content":[{"type":"text","text":"누구나 환영합니다."}]}]},
          {"type":"codeBlock","content":[{"type":"text","text":"fun main() = println(\"Hello\")"}]},
          {"type":"horizontalRule"},
          {"type":"paragraph","content":[{"type":"linkBookmark","attrs":{"href":"https://news.kaist.ac.kr/x","title":"https://news.kaist.ac.kr/x"}}]}
        ]}
        """
    )
)

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        TiptapView(previewLayout, onLinkClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePlaceholderPreview() {
    Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TiptapImagePlaceholder(failed = false)
            TiptapImagePlaceholder(failed = true)
        }
    }
}
