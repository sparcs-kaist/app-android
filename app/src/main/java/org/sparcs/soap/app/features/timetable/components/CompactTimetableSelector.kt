package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.navigationBar.animation.AnimatedText
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun CompactTimetableSelector(
    viewModel: TimetableViewModelProtocol,
    timetableName: String,
    modifier: Modifier = Modifier,
    isWide: Boolean = false
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isWide) Arrangement.SpaceBetween else Arrangement.spacedBy(8.dp)
    ) {
        SemesterSelector(viewModel = viewModel)

        TableSelector(
            viewModel = viewModel,
            displayName = timetableName,
            onRenameClick = {
                renameText = timetableName
                showRenameDialog = true
            }
        )
    }
    
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(text = stringResource(R.string.action_rename_user, timetableName)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = { Text(timetableName) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.renameTable(renameText)
                            showRenameDialog = false
                            renameText = ""
                        }
                    },
                    enabled = renameText.isNotBlank()
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        )
    }
}

@Composable
fun SemesterSelector(
    viewModel: TimetableViewModelProtocol,
) {
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val isEnabledPreviousButton =
        viewModel.semesters.collectAsState().value.firstOrNull() != selectedSemester
    val isEnabledNextButton =
        viewModel.semesters.collectAsState().value.lastOrNull() != selectedSemester
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .glassBorder(shape = RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
            contentDescription = "Select Previous Semester",
            tint = if (isEnabledPreviousButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (isEnabledPreviousButton) Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        coroutineScope.launch { viewModel.selectPreviousSemester() }
                    } else Modifier
                )
        )

        Spacer(Modifier.width(8.dp))

        AnimatedText(
            text = selectedSemester?.description ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = "Select Next Semester",
            tint = if (isEnabledNextButton) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (isEnabledNextButton) Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        coroutineScope.launch { viewModel.selectNextSemester() }
                    } else Modifier
                )
        )
    }
}

@Composable
fun TableSelector(
    viewModel: TimetableViewModelProtocol,
    displayName: String,
    onRenameClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .glassBorder(shape = RoundedCornerShape(25.dp))
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 25.dp, bottomStart = 25.dp))
                .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedText(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .size(16.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )

        Box {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Menu",
                modifier = Modifier
                    .clip(RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp))
                    .clickable { expanded = true }
                    .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                    .size(20.dp)
            )

            TimetableDropDownMenu(
                expanded = expanded,
                onDismiss = { expanded = false },
                onRenameClick = onRenameClick,
                viewModel = viewModel
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme { CompactTimetableSelector(viewModel = PreviewTimetableViewModel(), "") }
}
