package org.sparcs.soap.app.features.timetable.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCreationRoute(
    model: TimetableViewModelProtocol,
    timetableID: Int,
    activityID: Int?,
    onClose: () -> Unit,
) {
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
            try {
                update(model.timetableUseCase.getTable(timetableID, true))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                failed = true
            }
        }
    }
    val current = table
    if (current != null && (activityID == null || current.activities.any { it.id == activityID })) {
        ActivityCreationView(
            current, name, current.activities.find { it.id == activityID }, onClose,
            onSave = { update(model.timetableUseCase.saveActivity(timetableID, activityID, it)) },
            onRefresh = { update(model.timetableUseCase.getTable(timetableID, true)) })
    } else {
        Scaffold(topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            stringResource(R.string.activity_close),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                })
        }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (failed || current != null) {
                    Text(stringResource(R.string.activity_load_error))
                    TextButton(onClick = {
                        table = null; retry++
                    }) { Text(stringResource(R.string.activity_refresh)) }
                } else CircularProgressIndicator()
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        ActivityCreationRoute(
            model = PreviewTimetableViewModel(initialTimetable = Timetable.mock()),
            timetableID = 12,
            activityID = null,
            onClose = { }
        )
    }
}