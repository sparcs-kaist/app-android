package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.timetable.PaletteColor
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun ThemePaletteRow(
    color: PaletteColor,
    index: Int,
    count: Int,
    onEdit: () -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
    dragState: PaletteDragState,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember(color.id) { mutableStateOf(false) }
    val label = stringResource(R.string.theme_color, index + 1)
    val moveUp = stringResource(R.string.theme_move_color_up)
    val moveDown = stringResource(R.string.theme_move_color_down)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onEdit)
            .semantics {
                contentDescription = label
                customActions = buildList {
                    if (index > 0) add(CustomAccessibilityAction(moveUp) { onMove(-1); true })
                    if (index < count - 1) add(CustomAccessibilityAction(moveDown) { onMove(1); true })
                }
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp)
                        .pointerInput(color.id, dragState) {
                            detectDragGestures(
                                onDragStart = { dragState.start(color.id) },
                                onDragEnd = dragState::stop,
                                onDragCancel = dragState::stop,
                                onDrag = { change, amount ->
                                    change.consume()
                                    dragState.dragBy(amount.y)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DragHandle, stringResource(R.string.theme_drag_color, index + 1))
            }
            Box(
                Modifier.size(28.dp)
                    .background(TimetableTheme.color(color.hex), CircleShape)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label)
            Text(
                stringResource(R.string.theme_color_hex_value, color.hex),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.theme_options, label))
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.theme_remove_color)) },
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, null) },
                    enabled = count > TimetableTheme.minimumColors,
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "en")
@Preview(showBackground = true, locale = "ko")
@Composable
private fun ThemePaletteRowPreview() {
    Theme {
        val color = PaletteColor("preview", "307878")
        val listState = rememberLazyListState()
        val dragState = rememberPaletteDragState(listState, listOf(color.id)) { _, _ -> }
        ThemePaletteRow(
            color = color, index = 0, count = 1,
            onEdit = {}, onMove = {}, onDelete = {}, dragState = dragState
        )
    }
}
