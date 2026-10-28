package org.sparcs.soap.app.features.boardList.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.shared.views.contentViews.SkeletonBlock
import org.sparcs.soap.app.theme.ui.Theme

// Row widths cycle through these so the placeholder looks like board names without changing
// between recompositions.
private val rowWidths = listOf(0.45f, 0.3f, 0.6f, 0.4f)

/** Placeholder for the board list: one section header and [rowsPerGroup] board rows per group. */
fun LazyListScope.boardListSkeleton(rowsPerGroup: List<Int>) {
    rowsPerGroup.forEachIndexed { group, rows ->
        item(key = "skeleton-header-$group") { SkeletonHeader() }
        items(rows, key = { "skeleton-row-$group-$it" }) { row ->
            SkeletonRow(rowWidths[(group + row) % rowWidths.size])
        }
    }
}

@Composable
private fun SkeletonHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    ) {
        SkeletonBlock(Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        SkeletonBlock(Modifier.width(96.dp).height(14.dp))
    }
}

@Composable
private fun SkeletonRow(widthFraction: Float) {
    // Same height and inset as a one-line ListItem.
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
    ) {
        SkeletonBlock(Modifier.fillMaxWidth(widthFraction).height(16.dp))
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme { LazyColumn { boardListSkeleton(listOf(3, 2)) } }
}
