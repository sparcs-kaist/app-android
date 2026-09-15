package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityRefreshRequiredException
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.activity.activityTime
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

/** Shared by grid cells and the activity list so both use the same mutation/refresh flow. */
@Composable
internal fun ActivityDetailsDialog(
    activity: TimetableActivity?,
    showActions: Boolean,
    viewModel: TimetableViewModelProtocol,
    onEdit: (TimetableActivity) -> Unit,
    onDismiss: () -> Unit,
) {
    val timetable by viewModel.selectedTimetable.collectAsState()
    var deleting by remember { mutableStateOf(false) }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var failed by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (activity == null) return
    AlertDialog(
        onDismissRequest = { if (!deleting) onDismiss() },
        title = { Text(activity.title) },
        text = {
            Column {
                if (activity.location.isNotBlank()) Text(activity.location)
                DayType.fromValue(activity.day)?.let { Text(stringResource(it.stringValue)) }
                Text("${activityTime(activity.begin)} – ${activityTime(activity.end)}")
                if (failed) Text(
                    stringResource(if (refreshing) R.string.activity_saved_refresh else R.string.activity_save_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            if (deleting) CircularProgressIndicator()
            else if (showActions || refreshing) TextButton(onClick = {
                val tableID = timetable?.id?.toIntOrNull() ?: return@TextButton
                val useCase = viewModel.timetableUseCase ?: return@TextButton
                scope.launch {
                    deleting = true
                    try {
                        val fresh = if (refreshing) useCase.getTable(
                            tableID,
                            true
                        ) else useCase.deleteActivity(tableID, activity.id)
                        viewModel.activityTableUpdated(fresh)
                        refreshing = false
                        failed = false
                        onDismiss()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        refreshing = e is ActivityRefreshRequiredException || refreshing
                        failed = true
                    } finally {
                        deleting = false
                    }
                }
            }) { Text(stringResource(if (refreshing) R.string.activity_refresh else R.string.activity_delete)) }
            else TextButton(onClick = onDismiss) { Text(stringResource(R.string.activity_close)) }
        },
        dismissButton = {
            if (showActions && !refreshing) TextButton(
                enabled = !deleting,
                onClick = { onDismiss(); onEdit(activity) }) {
                Text(stringResource(R.string.activity_edit))
            }
        }
    )
}

@Composable
@Preview
private fun ActivityDetailsDialogPreview() {
    Theme {
        var showDialog by remember { mutableStateOf(true) }
        
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Button(onClick = { showDialog = true }) {
                Text("Show Dialog")
            }
            
            if (showDialog) {
                ActivityDetailsDialog(
                    activity = TimetableActivity(
                        id = 1,
                        title = "샘플 커스텀 일정",
                        location = "창의학습관 101호",
                        day = 1,
                        begin = 600,
                        end = 690
                    ),
                    showActions = true,
                    viewModel = PreviewTimetableViewModel(),
                    onEdit = {},
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

