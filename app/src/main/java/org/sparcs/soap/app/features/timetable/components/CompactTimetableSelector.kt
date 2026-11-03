package org.sparcs.soap.app.features.timetable.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.graphicsLayer
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
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.shared.views.contentViews.SkeletonLine
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
    val semester by viewModel.selectedSemester.collectAsState()
    val tableList by viewModel.timetableList.collectAsState()
    val tableID by viewModel.selectedTimetableID.collectAsState()
    // Until these load, the name would be a stand-in such as "My Table" or "Timetable #12".
    val isTableKnown = semester != null && (tableID == null || tableID == TimetableViewModel.MY_TABLE_ID ||
        tableList.any { it.id == tableID })
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
                onShareClick = sharesWithTip,
                isLoading = !isTableKnown
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
                }, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, shapes = ButtonDefaults.shapes()) {
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
                TextField(
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
                    enabled = renameText.isNotBlank() && !loadState.isReadOnly,
                    shapes = ButtonDefaults.shapes()
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }, shapes = ButtonDefaults.shapes()) {
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
    val isEnabledPreviousButton = selectedSemester != null && semesters.firstOrNull() != selectedSemester
    val isEnabledNextButton = selectedSemester != null && semesters.lastOrNull() != selectedSemester
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
                enabled = isEnabledPreviousButton,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Select Previous Semester"
                )
            }

            val semester = selectedSemester
            if (semester == null) {
                // Still loading: a placeholder where the name goes.
                Box(Modifier.width(labelWidth), contentAlignment = Alignment.Center) {
                    SkeletonLine(labelStyle, Modifier.width(labelWidth * 0.8f))
                }
            } else {
                Text(
                    text = semester.description,
                    style = labelStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(labelWidth)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    coroutineScope.launch { viewModel.selectNextSemester() }
                },
                enabled = isEnabledNextButton,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select Next Semester"
                )
            }
        }
    }
}

/**
 * The selected table as a Material 3 Expressive split button: the name switches between the
 * semester's tables, and the arrow opens what can be done with them.
 */
@Composable
fun TableSelector(
    viewModel: TimetableViewModelProtocol,
    displayName: String,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
) {
    var listExpanded by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    // The semester selector's pill color and height, so the two read as one row.
    val colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = CardDefaults.cardColors().containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val height = 48.dp

    SplitButtonLayout(
        leadingButton = {
            Box {
                SplitButtonDefaults.TonalLeadingButton(
                    onClick = { listExpanded = true },
                    enabled = !isLoading,
                    shapes = SplitButtonDefaults.leadingButtonShapesFor(height),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(height),
                    modifier = Modifier.height(height)
                ) {
                    if (isLoading) {
                        SkeletonLine(MaterialTheme.typography.titleSmall, Modifier.width(72.dp))
                    } else {
                        Text(
                            displayName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TimetableListMenu(
                    expanded = listExpanded,
                    onDismiss = { listExpanded = false },
                    viewModel = viewModel
                )
            }
        },
        trailingButton = {
            Box {
                SplitButtonDefaults.TonalTrailingButton(
                    checked = actionsExpanded,
                    onCheckedChange = { actionsExpanded = it },
                    enabled = !isLoading,
                    shapes = SplitButtonDefaults.trailingButtonShapesFor(height),
                    colors = colors,
                    contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(height),
                    modifier = Modifier.height(height)
                ) {
                    // Points up while its menu is open, as in the spec's split button.
                    val rotation by animateFloatAsState(if (actionsExpanded) 180f else 0f, label = "Arrow")
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.timetable_options, displayName),
                        modifier = Modifier
                            .size(SplitButtonDefaults.trailingButtonIconSizeFor(height))
                            .graphicsLayer { rotationZ = rotation }
                    )
                }
                TimetableActionsMenu(
                    expanded = actionsExpanded,
                    onDismiss = { actionsExpanded = false },
                    onRenameClick = onRenameClick,
                    onDeleteClick = onDeleteClick,
                    onShareClick = onShareClick,
                    viewModel = viewModel
                )
            }
        },
    )
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme { CompactTimetableSelector(viewModel = PreviewTimetableViewModel(), "") }
}

private const val SHARE_TIP_LECTURE_COUNT = 3

@Composable
@Preview(showBackground = true)
private fun LoadingPreview() {
    Theme { CompactTimetableSelector(viewModel = PreviewTimetableViewModel(initialSemester = null), "") }
}
