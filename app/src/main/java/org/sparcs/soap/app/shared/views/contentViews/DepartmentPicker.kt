package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.features.settings.timetable.InterestedDepartmentsSkeleton
import org.sparcs.soap.app.features.settings.timetable.TimetableSettingsViewModel
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun DepartmentPicker(
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
) {
    val viewModel: TimetableSettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchDepartments() }

    DepartmentPickerContent(
        state = state,
        selectedDepartmentIDs = selectedDepartmentIDs,
        onSelectionChange = onSelectionChange,
        onRetry = viewModel::fetchDepartments
    )
}

@Composable
fun DepartmentPickerContent(
    state: TimetableSettingsViewModel.ViewState,
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        TextButton(onClick = { onSelectionChange(emptySet()) }) {
            Text(stringResource(R.string.reset))
        }
        when (state) {
            is TimetableSettingsViewModel.ViewState.Loading -> {
                InterestedDepartmentsSkeleton()
            }

            is TimetableSettingsViewModel.ViewState.Error -> {
                ErrorView(
                    defaultMessageResId = R.string.department_load_failed,
                    error = state.error,
                    onRetry = onRetry
                )
            }

            is TimetableSettingsViewModel.ViewState.Loaded -> {
                DepartmentSelectionSections(
                    departments = state.departments,
                    interestedDepartmentIDs = state.savedDepartmentIDs,
                    selectedDepartmentIDs = selectedDepartmentIDs,
                    onSelectionChange = onSelectionChange
                )
            }
        }
    }
}

@Composable
fun DepartmentSelectionSections(
    departments: List<DepartmentOption>,
    interestedDepartmentIDs: Set<Int>,
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    enabled: Boolean = true,
) {
    var searchText by remember { mutableStateOf("") }
    val filtered = remember(departments, searchText) {
        departments.filter {
            it.name.contains(searchText.trim(), true) || it.code.contains(searchText.trim(), true)
        }
    }
    val groups = listOf(
        R.string.interested_departments to filtered.filter { it.id in interestedDepartmentIDs },
        R.string.other_departments to filtered.filter { it.id !in interestedDepartmentIDs },
    )
    Column {
        SearchCustomBar(
            value = searchText,
            onValueChange = { searchText = it },
            onValueClear = { searchText = "" },
            placeHolder = stringResource(R.string.department_search)
        )
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 600.dp),
                contentAlignment = Alignment.Center
            ) {
                UnavailableView(
                    icon = Icons.Rounded.ReportProblem,
                    title = stringResource(R.string.no_results),
                    description = stringResource(R.string.no_results),
                )
            }
        } else {
            LazyColumn(Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)) {
                groups.forEach { (title, options) ->
                    if (options.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(title),
                                Modifier.padding(12.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        items(options, key = { it.id }) { department ->
                            val selected = department.id in selectedDepartmentIDs
                            val toggle = {
                                onSelectionChange(
                                    if (selected) selectedDepartmentIDs - department.id
                                    else selectedDepartmentIDs + department.id
                                )
                            }
                            ListItem(
                                headlineContent = { Text(department.name) },
                                supportingContent = { Text(department.code) },
                                trailingContent = {
                                    Checkbox(
                                        selected,
                                        onCheckedChange = { toggle() },
                                        enabled = enabled
                                    )
                                },
                                modifier = Modifier.clickable(enabled = enabled, onClick = toggle),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DepartmentSelectionSectionsPreview() {
    Theme {
        DepartmentSelectionSections(
            listOf(
                DepartmentOption(1, "Computer Science", "CS"),
                DepartmentOption(2, "Mathematics", "MAS")
            ),
            setOf(1),
            setOf(1),
            {}
        )
    }
}