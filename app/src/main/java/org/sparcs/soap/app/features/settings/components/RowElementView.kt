package org.sparcs.soap.app.features.settings.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Read-only label/value row, e.g. "Nickname" over the user's nickname. [trailing] puts a short
 * value such as a count at the end of the row instead of under the label.
 */
@Composable
fun RowElementView(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    trailing: Boolean = false
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = if (trailing) null else {
            { Text(text = content) }
        },
        trailingContent = if (trailing) {
            { Text(text = content, style = MaterialTheme.typography.bodyLarge) }
        } else null,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
    )
}

@Composable
@Preview
private fun Preview() {
    Theme {
        LazyColumn {
            item {
                RowElementView(title = "Title", content = "Content")
            }
        }
    }
}
