package org.sparcs.soap.widgets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.timetable.displayName
import org.sparcs.soap.app.theme.ui.grayBB

/**
 * Palette picker for a widget's own configuration. A widget keeps the theme chosen here, which is
 * independent of the one the app's timetable uses.
 */
@Composable
fun WidgetPaletteRow(
    themes: List<TimetableTheme>,
    selectedID: String,
    onThemeSelected: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val selected = themes.firstOrNull { it.id == selectedID } ?: TimetableTheme.Default

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.padding(vertical = 8.dp).weight(1f)) {
            Text(
                text = stringResource(R.string.timetable_theme),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = selected.displayName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
            )
        }
        Swatches(selected)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text(stringResource(R.string.timetable_theme)) },
            text = {
                LazyColumn {
                    items(themes, key = { it.id }) { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeSelected(theme.id); showDialog = false }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = theme.id == selectedID,
                                onClick = { onThemeSelected(theme.id); showDialog = false }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.displayName(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.size(4.dp))
                                Swatches(theme)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun Swatches(theme: TimetableTheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        theme.hexColors.take(5).forEach { hex ->
            Box(Modifier.size(14.dp).background(TimetableTheme.color(hex), CircleShape))
        }
    }
}
