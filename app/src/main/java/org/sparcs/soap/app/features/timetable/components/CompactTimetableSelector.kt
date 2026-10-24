package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@Composable
fun CompactTimetableSelector(
    viewModel: TimetableViewModelProtocol,
    timetableName: String,
    modifier: Modifier = Modifier,
    isWide: Boolean = false,
    onShareClick: (() -> Unit)? = null,
    onDialogVisibilityChange: (Boolean) -> Unit = {},
) {
    val loadState by viewModel.loadState.collectAsState()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val currentOnDialogVisibilityChange by rememberUpdatedState(onDialogVisibilityChange)
    val isDialogPresented = showRenameDialog || showDeleteDialog
    LaunchedEffect(isDialogPresented) { currentOnDialogVisibilityChange(isDialogPresented) }
    DisposableEffect(Unit) { onDispose { currentOnDialogVisibilityChange(false) } }
    val tipStore = LocalTipStore.current
    val timetable by viewModel.selectedTimetable.collectAsState()
    val sharesWithTip = onShareClick?.let { share ->
        {
            tipStore?.invalidate(BuddyTip.ShareTimetable)
            share()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = if (isWide) Modifier.fillMaxWidth() else Modifier,
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
                },
                onDeleteClick = { showDeleteDialog = true },
                onShareClick = sharesWithTip
            )
        }

        TipView(
            BuddyTip.ShareTimetable,
            enabled = isWide && onShareClick != null && (timetable?.lectures?.size ?: 0) >= SHARE_TIP_LECTURE_COUNT,
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.timetable_delete_confirm_title, timetableName)) },
            text = { Text(stringResource(R.string.timetable_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTable()
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
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
                    enabled = renameText.isNotBlank() && !loadState.isReadOnly
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/** Previous and next buttons around the selected semester, in a pill like the cards below. */
@Composable
fun SemesterSelector(
    viewModel: TimetableViewModelProtocol,
) {
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val semesters by viewModel.semesters.collectAsState()
    val isEnabledPreviousButton = semesters.firstOrNull() != selectedSemester
    val isEnabledNextButton = semesters.lastOrNull() != selectedSemester
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Fixed to the widest semester name, so the pill and its arrows don't shift between
    // shorter and longer names such as Spring and Autumn.
    val labelStyle = MaterialTheme.typography.titleSmall
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val unknown = stringResource(R.string.unknown)
    val labels = semesters.map { it.description } + unknown
    val labelWidth = remember(labels, labelStyle, density) {
        val widest = labels.maxOf { measurer.measure(it, labelStyle).size.width }
        with(density) { widest.toDp() }
    }

    Card(shape = CircleShape) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    coroutineScope.launch { viewModel.selectPreviousSemester() }
                },
                enabled = isEnabledPreviousButton
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Select Previous Semester"
                )
            }

            Text(
                text = selectedSemester?.description ?: unknown,
                style = labelStyle,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(labelWidth)
            )

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    coroutineScope.launch { viewModel.selectNextSemester() }
                },
                enabled = isEnabledNextButton
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select Next Semester"
                )
            }
        }
    }
}

/** The selected table as a pill; tapping it opens the table menu. */
@Composable
fun TableSelector(
    viewModel: TimetableViewModelProtocol,
    displayName: String,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        // A pill matching the semester selector; tapping anywhere opens the table menu.
        Card(onClick = { expanded = true }, shape = CircleShape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .height(48.dp)
                    .padding(start = 16.dp, end = 12.dp)
            ) {
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Menu")
            }
        }

        TimetableDropDownMenu(
            expanded = expanded,
            onDismiss = { expanded = false },
            onRenameClick = onRenameClick,
            onDeleteClick = onDeleteClick,
            onShareClick = onShareClick,
            viewModel = viewModel
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme { CompactTimetableSelector(viewModel = PreviewTimetableViewModel(), "") }
}

private const val SHARE_TIP_LECTURE_COUNT = 3
