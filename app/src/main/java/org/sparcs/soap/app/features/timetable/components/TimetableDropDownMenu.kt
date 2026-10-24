package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun TimetableDropDownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: TimetableViewModelProtocol,
    onShareClick: (() -> Unit)? = null,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        TimetableListItems(viewModel, onDismiss)

        TimetableManagementItems(viewModel, onDismiss, onRenameClick, onDeleteClick, onShareClick)
    }
}

@Composable
fun TimetableListItems(
    viewModel: TimetableViewModelProtocol,
    onDismiss: () -> Unit,
) {
    val selectedId by viewModel.selectedTimetableID.collectAsState()
    val timetableList by viewModel.timetableList.collectAsState()
    val scope = rememberCoroutineScope()

    val isMyTableSelected = selectedId == null || selectedId == -1

    DropdownMenuItem(
        text = { Text(stringResource(R.string.my_table)) },
        onClick = {
            scope.launch { viewModel.selectTimetable(-1) }
            onDismiss()
        },
        leadingIcon = {
            if (isMyTableSelected) Icon(Icons.Default.Check, contentDescription = "Selected")
        }
    )

    timetableList.forEach { timetableInfo ->
        val isSelected = selectedId == timetableInfo.id

        DropdownMenuItem(
            text = {
                Text(timetableInfo.title.ifEmpty { stringResource(R.string.untitled) })
            },
            onClick = {
                scope.launch {
                    viewModel.selectTimetable(timetableInfo.id)
                }
                onDismiss()
            },
            leadingIcon = {
                if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected")
            }
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

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_add)) },
        enabled = !loadState.isReadOnly,
        onClick = {
            scope.launch {
                viewModel.createTable()
            }
            onDismiss()
        },
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
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = null
            )
        },
        enabled = !isDuplicating && !loadState.isReadOnly
    )

    HorizontalDivider()

    if (onShareClick != null) {
        val semester by viewModel.selectedSemester.collectAsState()
        val timetable by viewModel.selectedTimetable.collectAsState()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.timetable_share)) },
            leadingIcon = { Icon(Icons.Outlined.Share, null) },
            enabled = semester != null && timetable != null,
            onClick = { onDismiss(); onShareClick() },
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
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null
            )
        },
        enabled = isActionEnabled
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_delete)) },
        onClick = {
            onDismiss()
            if (isActionEnabled) {
                onDeleteClick()
            }
        },
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

@Composable
@Preview
private fun Preview() {
    Theme {
        Box(Modifier.fillMaxSize()) {
            Button(
                onClick = {}
            ) {
                TimetableDropDownMenu(
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