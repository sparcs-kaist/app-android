package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.app.theme.ui.lightGray0
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
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(16.dp)
    ) {
        TimetableListItems(viewModel, onDismiss)

        TimetableManagementItems(viewModel, onDismiss, onRenameClick, onDeleteClick, onShareClick)
    }
}

@Composable
private fun TopDropDownItems() { //지도, 시험 시간표(바꿀 수 있도록)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconWithText(
            icon = Icons.Outlined.TableChart,
            text = stringResource(R.string.timetable)
        )//기본

        IconWithText(
            icon = Icons.Outlined.Description,
            text = stringResource(R.string.timetable)
        )//시험

        IconWithText(
            icon = Icons.Outlined.LocationOn,
            text = stringResource(R.string.timetable)
        )//지도
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.lightGray0,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun IconWithText(
    icon: ImageVector,
    text: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
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
    val isActionEnabled = selectedTimetableID != null

    val deleteColor =
        if (isActionEnabled) Color(0xFFE54C65) else MaterialTheme.colorScheme.grayBB.copy(alpha = 0.5f)
    val renameColor =
        if (isActionEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.grayBB.copy(
            alpha = 0.5f
        )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_add)) },
        onClick = {
            scope.launch {
                viewModel.createTable()
            }
            onDismiss()
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Add,
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
        enabled = !isDuplicating
    )

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    )

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
        text = { Text(stringResource(R.string.timetable_rename), color = renameColor) },
        onClick = {
            onDismiss()
            if (isActionEnabled) {
                onRenameClick()
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = renameColor
            )
        },
        enabled = isActionEnabled
    )

    DropdownMenuItem(
        text = { Text(stringResource(R.string.timetable_delete), color = deleteColor) },
        onClick = {
            onDismiss()
            if (isActionEnabled) {
                onDeleteClick()
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = deleteColor
            )
        },
        enabled = isActionEnabled
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