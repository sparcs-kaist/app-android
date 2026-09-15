package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme

@Composable
fun ThemeRow(
    theme: TimetableTheme,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Surface(
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .weight(1f)
                    .selectable(selected, role = Role.RadioButton, onClick = onSelect)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected, onClick = null)
                Column(
                    Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        theme.displayName(),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        theme.hexColors.take(7).forEach {
                            Box(
                                Modifier
                                    .size(16.dp)
                                    .background(TimetableTheme.color(it), CircleShape)
                            )
                        }
                    }
                }
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        stringResource(R.string.theme_options, theme.displayName())
                    )
                }
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    if (!theme.isBuiltIn) DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = { menu = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.theme_duplicate)) },
                        onClick = { menu = false; onDuplicate() })
                    if (!theme.isBuiltIn) DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeRowPreview() {
    MaterialTheme {
        Box(Modifier.padding(16.dp)) {
            ThemeRow(
                theme = TimetableTheme.Default,
                selected = true,
                onSelect = {},
                onEdit = {},
                onDuplicate = {},
                onDelete = {}
            )
        }
    }
}
