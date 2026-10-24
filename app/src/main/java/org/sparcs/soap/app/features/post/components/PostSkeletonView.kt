package org.sparcs.soap.app.features.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

// Placeholders for the post screen. Each one mirrors the layout it stands in for, using the same
// text styles, avatar sizes and pill heights, so nothing jumps when the post loads.

@Composable
fun PostViewSkeleton() {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        item { HeaderSkeleton() }
        item { ContentSkeleton() }
        item { FooterSkeleton() }
        items(2) { CommentSkeleton() }
    }
}

/** Mirrors PostHeader: title, date and views, author row, divider. */
@Composable
fun HeaderSkeleton() {
    val typography = MaterialTheme.typography
    Column {
        LinePlaceholder(typography.titleLarge, Modifier.fillMaxWidth(0.8f))
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LinePlaceholder(typography.bodySmall, Modifier.width(160.dp))
            LinePlaceholder(typography.bodySmall, Modifier.width(48.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Placeholder(Modifier.size(32.dp), CircleShape)
            LinePlaceholder(typography.titleSmall, Modifier.width(96.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun ContentSkeleton() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        listOf(1f, 1f, 0.9f, 1f, 0.6f).forEach { fraction ->
            LinePlaceholder(MaterialTheme.typography.bodyLarge, Modifier.fillMaxWidth(fraction))
        }
    }
}

/** Mirrors PostFooter: vote and comment pills, then bookmark and share. */
@Composable
fun FooterSkeleton() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Placeholder(Modifier.width(112.dp).height(pillHeight(false)), CircleShape)
        Placeholder(Modifier.width(64.dp).height(pillHeight(false)), CircleShape)
        Spacer(Modifier.weight(1f))
        Placeholder(Modifier.size(pillHeight(false)), CircleShape)
        Placeholder(Modifier.size(pillHeight(false)), CircleShape)
    }
}

/** Mirrors PostCommentCell: divider, author row with the overflow button, text, pills. */
@Composable
fun CommentSkeleton() {
    val typography = MaterialTheme.typography
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // 48dp: the height of the overflow IconButton in a loaded comment.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Placeholder(Modifier.size(24.dp), CircleShape)
            LinePlaceholder(typography.bodyMedium, Modifier.width(120.dp))
        }
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            LinePlaceholder(typography.bodyMedium, Modifier.fillMaxWidth())
            LinePlaceholder(typography.bodyMedium, Modifier.fillMaxWidth(0.5f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, end = 4.dp)
        ) {
            Placeholder(Modifier.width(56.dp).height(pillHeight(true)), CircleShape)
            Placeholder(Modifier.width(96.dp).height(pillHeight(true)), CircleShape)
        }
    }
}

/** A bar as tall as the font, centered in a box one line of [style] tall. */
@Composable
private fun LinePlaceholder(style: TextStyle, modifier: Modifier) {
    val density = LocalDensity.current
    val lineHeight: Dp = with(density) { style.lineHeight.toDp() }
    val barHeight: Dp = with(density) { style.fontSize.toDp() }
    Box(contentAlignment = Alignment.CenterStart, modifier = modifier.height(lineHeight)) {
        Placeholder(Modifier.fillMaxWidth().height(barHeight))
    }
}

@Composable
private fun Placeholder(modifier: Modifier, shape: Shape = RoundedCornerShape(4.dp)) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, shape))
}

@Preview(showBackground = true)
@Composable
private fun PreviewSkeleton() {
    Theme { PostViewSkeleton() }
}
