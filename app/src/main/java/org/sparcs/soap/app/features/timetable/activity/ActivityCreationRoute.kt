package org.sparcs.soap.app.features.timetable.activity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CancellationException
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.features.timetable.TimetableViewModelProtocol
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipEvent
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewActivityParsingUseCase
import org.sparcs.soap.buddyPreviewSupport.otl.PreviewTimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCreationRoute(
    model: TimetableViewModelProtocol,
    timetableID: Int,
    activityID: Int?,
    navController: NavController,
    quickFillViewModel: ActivityQuickFillViewModel = hiltViewModel(),
) {
    val onClose: () -> Unit = { navController.popBackStack() }
    val quickFill by quickFillViewModel.state.collectAsState()
    val selected by model.selectedTimetable.collectAsState()
    val selectedName by model.timetableName.collectAsState()
    val name by rememberSaveable { mutableStateOf(selectedName) }
    var table by remember { mutableStateOf(selected?.takeIf { it.id == timetableID.toString() }) }
    var loadError by remember { mutableStateOf<Exception?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    val update: (Timetable) -> Unit = { table = it }
    val tipStore = LocalTipStore.current
    LaunchedEffect(Unit) { if (activityID == null) tipStore?.donate(TipEvent.ActivityCreationOpened) }
    LaunchedEffect(timetableID, retry) {
        if (table == null) {
            loadError = null
            try {
                update(model.refreshActivityTable(timetableID))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loadError = e
            }
        }
    }
    val current = table
    if (current != null && (activityID == null || current.activities.any { it.id == activityID })) {
        ActivityCreationView(
            current, name, current.activities.find { it.id == activityID }, onClose,
            onSave = { update(model.saveActivity(timetableID, activityID, it)) },
            onRefresh = { update(model.refreshActivityTable(timetableID)) },
            quickFill = quickFill,
            onQuickFill = quickFillViewModel::parse,
            onQuickFillTextChange = quickFillViewModel::clearError,
        )
    } else {
        Scaffold(
            modifier = Modifier.analyticsScreen("ActivityCreationLoading"),
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.activity_close)
                            )
                        }
                    })
        }) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                // Loaded, but the activity being edited is gone: treat it like a failed load.
                val error = loadError ?: if (current != null) IllegalStateException("Activity not found") else null
                if (error != null) {
                    ErrorView(
                        error = error,
                        defaultMessageResId = R.string.activity_load_error,
                        onRetry = { table = null; retry++ }
                    )
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
            navController = rememberNavController(),
            quickFillViewModel = ActivityQuickFillViewModel(PreviewActivityParsingUseCase()),
        )
    }
}