package org.sparcs.soap.app.features.settings.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.settings.components.LocalSettingsSegment
import org.sparcs.soap.app.features.settings.components.SettingsSegment
import org.sparcs.soap.app.features.settings.components.segmentedShapes
import org.sparcs.soap.app.features.settings.components.settingsSegmentedColors
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun ThemeRow(
    theme: TimetableTheme,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit = {},
) {
    var menu by remember { mutableStateOf(false) }
    val tipStore = LocalTipStore.current
    val segment = LocalSettingsSegment.current ?: SettingsSegment(0, 1)
    // The whole row selects the theme; the radio shows which one is in use.
    SegmentedListItem(
        onClick = onSelect,
        shapes = segmentedShapes(segment),
        colors = settingsSegmentedColors(),
        modifier = Modifier.semantics {
            role = Role.RadioButton
            this.selected = selected
        },
        leadingContent = { RadioButton(selected, onClick = null) },
        supportingContent = {
            Row(
                Modifier
                    .padding(top = 4.dp)
                    .width(144.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                theme.hexColors.forEach {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(TimetableTheme.color(it))
                    )
                }
            }
        },
        trailingContent = {
            Box {
                IconButton(
                    onClick = {
                        tipStore?.invalidate(BuddyTip.ThemeActions)
                        menu = true
                    },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        stringResource(R.string.theme_options, theme.displayName()),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Groups: sharing and editing the theme, then deleting it on its own.
                ExpressiveMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false }
                ) {
                    val groupCount = if (theme.isBuiltIn) 1 else 2
                    val actionCount = if (theme.isBuiltIn) 2 else 3
                    MenuGroup(index = 0, count = groupCount) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.theme_share)) },
                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                            onClick = { menu = false; onShare() },
                            shape = menuItemShape(0, actionCount))
                        if (!theme.isBuiltIn) DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = { menu = false; onEdit() },
                            shape = menuItemShape(1, actionCount))
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.theme_duplicate)) },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                            onClick = { menu = false; onDuplicate() },
                            shape = menuItemShape(actionCount - 1, actionCount))
                    }
                    if (!theme.isBuiltIn) {
                        MenuGroup(index = 1, count = groupCount) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                                onClick = { menu = false; onDelete() },
                                shape = menuItemShape(0, 1))
                        }
                    }
                }
            }
        }
    ) {
        Text(
            theme.displayName(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeRowPreview() {
    Theme {
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
