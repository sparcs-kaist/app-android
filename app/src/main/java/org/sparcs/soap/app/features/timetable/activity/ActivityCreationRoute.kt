package org.sparcs.soap.app.features.timetable.activity

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.timetable.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCreationRoute(model: TimetableViewModel, timetableID: Int, activityID: Int?, onClose: () -> Unit) {
    val selected by model.selectedTimetable.collectAsState()
    val selectedName by model.timetableName.collectAsState()
    val name by rememberSaveable { mutableStateOf(selectedName) }
    var table by remember { mutableStateOf(selected?.takeIf { it.id == timetableID.toString() }) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val update: (Timetable) -> Unit = { table = it; model.activityTableUpdated(it) }
    LaunchedEffect(timetableID, retry) {
        if (table == null) {
            failed = false
            try { update(model.timetableUseCase.getTable(timetableID, true)) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { failed = true }
        }
    }
    val current = table
    if (current != null && (activityID == null || current.activities.any { it.id == activityID })) {
        ActivityCreationView(current, name, current.activities.find { it.id == activityID }, onClose,
            onSave = { update(model.timetableUseCase.saveActivity(timetableID, activityID, it)) },
            onRefresh = { update(model.timetableUseCase.getTable(timetableID, true)) })
    } else {
        Scaffold(topBar = { TopAppBar(title = {}, navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.Close, stringResource(R.string.activity_close)) } }) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                if (failed || current != null) {
                    Text(stringResource(R.string.activity_load_error))
                    TextButton(onClick = { table = null; retry++ }) { Text(stringResource(R.string.activity_refresh)) }
                } else CircularProgressIndicator()
            }
        }
    }
}
