package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.menuItemShape
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

/** The semester's tables to switch between, opened from the table name. */
@Composable
fun TimetableListMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    viewModel: TimetableViewModelProtocol,
) {
    ExpressiveMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        MenuGroup(index = 0, count = 1, maxHeight = TimetableListMaxHeight) {
            TimetableListItems(viewModel, onDismiss)
        }
    }
}

/** What can be done with the tables, opened from the arrow beside the table name. */
@Composable
fun TimetableActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: TimetableViewModelProtocol,
    onShareClick: (() -> Unit)? = null,
) {
    // Groups: creating a table, acting on the selected one, and deleting it.
    ExpressiveMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        TimetableManagementItems(viewModel, onDismiss, onRenameClick, onDeleteClick, onShareClick)
    }
}

private val TimetableListMaxHeight = 320.dp

@Composable
fun TimetableListItems(
    viewModel: TimetableViewModelProtocol,
    onDismiss: () -> Unit,
) {
    val selectedId by viewModel.selectedTimetableID.collectAsState()
    val timetableList by viewModel.timetableList.collectAsState()
    val scope = rememberCoroutineScope()

    val isMyTableSelected = selectedId == null || selectedId == -1
    val count = timetableList.size + 1
    val checkIcon: @Composable () -> Unit = {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
    }

    SelectableDropdownMenuItem(
        colors = selectableMenuItemColors(),
        selected = isMyTableSelected,
        text = { Text(stringResource(R.string.my_table)) },
        onClick = {
            scope.launch { viewModel.selectTimetable(-1) }
            onDismiss()
        },
        shapes = MenuDefaults.itemShape(0, count),
        selectedLeadingIcon = checkIcon,
    )

    timetableList.forEachIndexed { index, timetableInfo ->
        SelectableDropdownMenuItem(
            colors = selectableMenuItemColors(),
            selected = selectedId == timetableInfo.id,
            text = {
                Text(timetableInfo.title.ifEmpty { stringResource(R.string.untitled) })
            },
            onClick = {
                scope.launch {
                    viewModel.selectTimetable(timetableInfo.id)
                }
                onDismiss()
            },
            shapes = MenuDefaults.itemShape(index + 1, count),
            selectedLeadingIcon = checkIcon,
        )
    }
}

@Composable
private fun TimetableManagementItems(
    viewModel: TimetableViewModelProtocol,
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: (() -> Unit)?,
) {
    val scope = rememberCoroutineScope()
    val selectedTimetableID by viewModel.selectedTimetableID.collectAsState()
    val isDuplicating by viewModel.isDuplicatingTable.collectAsState()
    val loadState by viewModel.loadState.collectAsState()
    val isActionEnabled = selectedTimetableID != null && !loadState.isReadOnly

    // Delete reads in the error color; disabled items use Material's own dimming.
    val deleteColors = MenuDefaults.itemColors(
        textColor = MaterialTheme.colorScheme.error,
        leadingIconColor = MaterialTheme.colorScheme.error
    )

    MenuGroup(index = 0, count = 3) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.timetable_add)) },
            enabled = !loadState.isReadOnly,
            onClick = {
                scope.launch {
                    viewModel.createTable()
                }
                onDismiss()
            },
            shape = menuItemShape(0, 2),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null
                )
            }
        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.timetable_duplicate)) },
            onClick = {
                viewModel.duplicateMyTable()
                onDismiss()
            },
            shape = menuItemShape(1, 2),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null
                )
            },
            enabled = !isDuplicating && !loadState.isReadOnly
        )
    }

    MenuGroup(index = 1, count = 3) {
        val actionCount = if (onShareClick != null) 2 else 1
        if (onShareClick != null) {
            val semester by viewModel.selectedSemester.collectAsState()
            val timetable by viewModel.selectedTimetable.collectAsState()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.timetable_share)) },
                leadingIcon = { Icon(Icons.Outlined.Share, null) },
                enabled = semester != null && timetable != null,
                onClick = { onDismiss(); onShareClick() },
                shape = menuItemShape(0, actionCount),
            )
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.timetable_rename)) },
            onClick = {
                onDismiss()
                if (isActionEnabled) {
                    onRenameClick()
                }
            },
            shape = menuItemShape(actionCount - 1, actionCount),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null
                )
            },
            enabled = isActionEnabled
        )
    }

    MenuGroup(index = 2, count = 3) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.timetable_delete)) },
            onClick = {
                onDismiss()
                if (isActionEnabled) {
                    onDeleteClick()
                }
            },
            shape = menuItemShape(0, 1),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
            },
            enabled = isActionEnabled,
            colors = deleteColors
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        Box(Modifier.fillMaxSize()) {
            Button(
                onClick = {},
                shapes = ButtonDefaults.shapes()
            ) {
                TimetableActionsMenu(
                    expanded = true,
                    onDismiss = {},
                    onRenameClick = {},
                    onDeleteClick = {},
                    viewModel = PreviewTimetableViewModel()
                )
            }
        }
    }
}