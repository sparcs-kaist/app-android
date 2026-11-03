package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.theme.ui.Theme

/**
 * A titled section as one Material 3 Expressive segmented group: the [title] (with an optional
 * [icon]) above [count] rows, where the first and last rows take the group's rounded corners and
 * the rows between are split by small gaps. For screens that already inset their content, unlike
 * the Settings groups; the title lines up with the rows' content. A row that handles taps can
 * pass a zero [contentPadding] and pad itself, so its ripple fills the tile.
 */
@Composable
fun SegmentedSection(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    row: @Composable (index: Int) -> Unit,
) {
    // The default card color, so groups sit with the cards beside them.
    val color = CardDefaults.cardColors().containerColor
    Column(modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            if (icon != null) Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            repeat(count) { index ->
                Surface(
                    shape = ListItemDefaults.segmentedShapes(index, count).shape,
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier.padding(contentPadding)) { row(index) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        SegmentedSection("Graduation", count = 2, icon = Icons.Outlined.School) {
            Text("Row ${it + 1}")
        }
    }
}
