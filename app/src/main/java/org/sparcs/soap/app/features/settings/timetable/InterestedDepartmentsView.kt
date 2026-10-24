package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.DepartmentList
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun InterestedDepartmentsView(
    navController: NavController,
    viewModel: TimetableSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }
    InterestedDepartmentsContent(
        state = state,
        onSelectionChange = viewModel::selectDepartments,
        onSave = { viewModel.save { navController.popBackStack() } },
        onRetry = viewModel::load,
        onDismiss = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InterestedDepartmentsContent(
    state: TimetableSettingsViewModel.ViewState,
    onSelectionChange: (Set<Int>) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val loaded = state as? TimetableSettingsViewModel.ViewState.Loaded
    val isEditable = loaded != null
    val isDoneEnabled = loaded?.hasChanges == true && !loaded.isSaving

    Scaffold(
        modifier = Modifier.analyticsScreen("Interested Departments"),
        topBar = {
            Column {
                SettingsViewNavigationBar(
                    title = stringResource(R.string.interested_departments),
                    onDismiss = onDismiss,
                    isEditable = isEditable,
                    isDoneEnabled = isDoneEnabled,
                    isSaving = loaded?.isSaving == true,
                    onClickDone = onSave,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
                if (loaded?.isSaving == true) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        DepartmentList(
            state = state,
            selectedDepartmentIDs = loaded?.selectedDepartmentIDs.orEmpty(),
            onSelectionChange = onSelectionChange,
            onRetry = onRetry,
            errorMessage = R.string.department_save_load_failed,
            enabled = loaded?.isSaving != true,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(key = "description") {
                Text(
                    stringResource(R.string.interested_departments_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InterestedDepartmentsViewPreview() {
    Theme {
        InterestedDepartmentsContent(
            TimetableSettingsViewModel.ViewState.Loaded(
                departments = listOf(
                    DepartmentOption(1, "Computer Science", "CS"),
                    DepartmentOption(2, "Mathematics", "MAS"),
                    DepartmentOption(3, "Physics", "PH"),
                    DepartmentOption(4, "Electrical Engineering", "EE"),
                ),
                savedDepartmentIDs = setOf(1, 2),
                selectedDepartmentIDs = setOf(1, 2, 4),
            ),
            {},
            {},
            {},
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InterestedDepartmentsLoadingPreview() {
    Theme {
        InterestedDepartmentsContent(
            TimetableSettingsViewModel.ViewState.Loading,
            {},
            {},
            {},
            {}
        )
    }
}