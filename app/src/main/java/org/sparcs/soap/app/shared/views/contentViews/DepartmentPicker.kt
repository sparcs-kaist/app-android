package org.sparcs.soap.app.shared.views.contentViews

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.timetable.TimetableSettingsViewModel
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun DepartmentPicker(
    selectedDepartments: List<DepartmentOption>,
    onSelectionChange: (List<DepartmentOption>) -> Unit,
) {
    val viewModel: TimetableSettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.fetchDepartments() }

    DepartmentPickerContent(
        state = state,
        selectedDepartmentIDs = selectedDepartments.map { it.id }.toSet(),
        onSelectionChange = { ids ->
            val departments = (state as? TimetableSettingsViewModel.ViewState.Loaded)?.departments.orEmpty()
            onSelectionChange(departments.filter { it.id in ids })
        },
        onRetry = viewModel::fetchDepartments
    )
}

/** Department filter shown in the course search filter sheet. */
@Composable
fun DepartmentPickerContent(
    state: TimetableSettingsViewModel.ViewState,
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    onRetry: () -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp)
        ) {
            val count = selectedDepartmentIDs.size
            Text(
                text = stringResource(R.string.department) +
                    if (count > 0) " " + stringResource(R.string.selected_count, count) else "",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { onSelectionChange(emptySet()) },
                enabled = selectedDepartmentIDs.isNotEmpty()
            ) {
                Text(stringResource(R.string.reset))
            }
        }
        DepartmentList(
            state = state,
            selectedDepartmentIDs = selectedDepartmentIDs,
            onSelectionChange = onSelectionChange,
            onRetry = onRetry,
            errorMessage = R.string.department_load_failed,
        )
    }
}

/**
 * Searchable, multi-select department list. Departments the user marked as interested come
 * first under their own header; searching shows exact code matches first. [header] adds items
 * above the search bar, such as a description.
 */
@Composable
fun DepartmentList(
    state: TimetableSettingsViewModel.ViewState,
    selectedDepartmentIDs: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    onRetry: () -> Unit,
    @StringRes errorMessage: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    header: LazyListScope.() -> Unit = {},
) {
    var searchText by remember { mutableStateOf("") }
    val keyword = searchText.trim()
    val loaded = state as? TimetableSettingsViewModel.ViewState.Loaded
    val groups = remember(loaded?.departments, loaded?.savedDepartmentIDs, keyword) {
        loaded?.let { departmentGroups(it.departments, it.savedDepartmentIDs, keyword) }.orEmpty()
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 16.dp)) {
        header()

        when (state) {
            is TimetableSettingsViewModel.ViewState.Loading -> departmentListSkeleton()

            is TimetableSettingsViewModel.ViewState.Error -> item(key = "error") {
                ErrorView(
                    defaultMessageResId = errorMessage,
                    error = state.error,
                    onRetry = onRetry,
                )
            }

            is TimetableSettingsViewModel.ViewState.Loaded -> {
                item(key = "search") {
                    DepartmentSearchField(searchText, onQueryChange = { searchText = it })
                }
                if (groups.all { it.second.isEmpty() }) {
                    item(key = "empty") { NoDepartmentsFound() }
                }
                groups.forEach { (title, options) ->
                    if (options.isEmpty()) return@forEach
                    if (title != null) {
                        item(key = "header-$title") { SettingsSectionHeader(stringResource(title)) }
                    }
                    items(options, key = { it.id }) { department ->
                        val selected = department.id in selectedDepartmentIDs
                        ListItem(
                            headlineContent = { Text(department.name) },
                            supportingContent = { Text(department.code) },
                            leadingContent = {
                                Checkbox(checked = selected, onCheckedChange = null, enabled = enabled)
                            },
                            // Transparent so the list works on a screen or inside a sheet.
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.toggleable(
                                value = selected,
                                enabled = enabled,
                                role = Role.Checkbox,
                                onValueChange = { checked ->
                                    onSelectionChange(
                                        if (checked) selectedDepartmentIDs + department.id
                                        else selectedDepartmentIDs - department.id
                                    )
                                }
                            ),
                        )
                    }
                }
            }
        }
    }
}

/** Material docked search bar used as a plain search field (it never expands). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartmentSearchField(query: String, onQueryChange: (String) -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    DockedSearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { onQueryChange(it.replace("\n", "")) },
                onSearch = { keyboard?.hide() },
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(R.string.department_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search_text))
                        }
                    }
                } else null,
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {}
}

@Composable
private fun NoDepartmentsFound() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp)
            .padding(16.dp)
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Row widths cycle through these so the placeholder looks like names without changing between
// recompositions.
private val skeletonWidths = listOf(120.dp, 180.dp, 150.dp)

/** Placeholder mirroring the loaded list: search bar, then a header and rows per section. */
@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.departmentListSkeleton() {
    item(key = "skeleton-search") {
        Placeholder(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .height(SearchBarDefaults.InputFieldHeight),
            shape = SearchBarDefaults.dockedShape
        )
    }
    repeat(2) { section ->
        item(key = "skeleton-header-$section") {
            Placeholder(
                Modifier
                    .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    .width(100.dp)
                    .height(14.dp)
            )
        }
        items(3, key = { "skeleton-row-$section-$it" }) { row ->
            // Same height and inset as a two-line ListItem with a leading checkbox.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    Placeholder(Modifier.size(18.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Placeholder(Modifier.width(skeletonWidths[row % skeletonWidths.size]).height(16.dp))
                    Placeholder(Modifier.width(40.dp).height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Placeholder(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, shape))
}

private fun departmentGroups(
    departments: List<DepartmentOption>,
    interestedDepartmentIDs: Set<Int>,
    keyword: String,
): List<Pair<Int?, List<DepartmentOption>>> {
    if (keyword.isNotEmpty()) {
        val (exactCodes, others) = departments
            .filter { it.name.contains(keyword, true) || it.code.contains(keyword, true) }
            .partition { it.code.equals(keyword, true) }
        return listOf(null to exactCodes + others)
    }
    val (interested, others) = departments.partition { it.id in interestedDepartmentIDs }
    if (interested.isEmpty()) return listOf(null to others)
    return listOf(
        R.string.interested_departments to interested,
        R.string.other_departments to others,
    )
}

@Preview(showBackground = true)
@Composable
private fun DepartmentPickerContentPreview() {
    Theme {
        DepartmentPickerContent(
            state = TimetableSettingsViewModel.ViewState.Loaded(
                departments = listOf(
                    DepartmentOption(1, "Computer Science", "CS"),
                    DepartmentOption(2, "Mathematics", "MAS")
                ),
                savedDepartmentIDs = setOf(2)
            ),
            selectedDepartmentIDs = setOf(1),
            onSelectionChange = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DepartmentListLoadingPreview() {
    Theme {
        DepartmentList(
            state = TimetableSettingsViewModel.ViewState.Loading,
            selectedDepartmentIDs = emptySet(),
            onSelectionChange = {},
            onRetry = {},
            errorMessage = R.string.department_load_failed
        )
    }
}
