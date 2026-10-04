package org.sparcs.soap.app.features.settings.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.features.settings.components.SettingsViewNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.DepartmentSelectionSections
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
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
                )
                if (loaded?.isSaving == true) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.interested_departments_description),
                Modifier.padding(bottom = 16.dp)
            )

            when (state) {
                is TimetableSettingsViewModel.ViewState.Loading -> {
                    InterestedDepartmentsSkeleton()
                }

                is TimetableSettingsViewModel.ViewState.Error -> {
                    ErrorView(
                        defaultMessageResId = R.string.department_save_load_failed,
                        error = state.error,
                        onRetry = onRetry,
                    )
                }

                is TimetableSettingsViewModel.ViewState.Loaded -> {
                    DepartmentSelectionSections(
                        state.departments,
                        state.savedDepartmentIDs,
                        state.selectedDepartmentIDs,
                        onSelectionChange,
                        enabled = !state.isSaving,
                    )
                }
            }
        }
    }
}

@Composable
fun InterestedDepartmentsSkeleton() {
    val skeletonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(skeletonColor, RoundedCornerShape(12.dp))
        )

        repeat(2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(100.dp)
                        .height(18.dp)
                        .background(skeletonColor, RoundedCornerShape(4.dp))
                )

                repeat(3) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width((100 + (index % 3) * 30).dp)
                                    .height(16.dp)
                                    .background(skeletonColor, RoundedCornerShape(4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(12.dp)
                                    .background(skeletonColor, RoundedCornerShape(4.dp))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                                .background(skeletonColor, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InterestedDepartmentsViewPreview() {
    Theme {
        InterestedDepartmentsContent(
            TimetableSettingsViewModel.ViewState.Loaded(),
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