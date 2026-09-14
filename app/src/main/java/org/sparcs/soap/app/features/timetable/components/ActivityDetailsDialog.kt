package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityRefreshRequiredException
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.features.timetable.activity.activityTime

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
                if (failed) Text(stringResource(if (refreshing) R.string.activity_saved_refresh else R.string.activity_save_error), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            if (deleting) CircularProgressIndicator()
            else if (showActions || refreshing) TextButton(onClick = {
                val tableID = timetable?.id?.toIntOrNull() ?: return@TextButton
                val useCase = viewModel.timetableUseCase ?: return@TextButton
                scope.launch {
                    deleting = true
                    try {
                        val fresh = if (refreshing) useCase.getTable(tableID, true) else useCase.deleteActivity(tableID, activity.id)
                        viewModel.activityTableUpdated(fresh)
                        refreshing = false
                        failed = false
                        onDismiss()
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) {
                        refreshing = e is ActivityRefreshRequiredException || refreshing
                        failed = true
                    } finally { deleting = false }
                }
            }) { Text(stringResource(if (refreshing) R.string.activity_refresh else R.string.activity_delete)) }
            else TextButton(onClick = onDismiss) { Text(stringResource(R.string.activity_close)) }
        },
        dismissButton = {
            if (showActions && !refreshing) TextButton(enabled = !deleting, onClick = { onDismiss(); onEdit(activity) }) {
                Text(stringResource(R.string.activity_edit))
            }
        }
    )
}
