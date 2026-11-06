package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterOption
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun getTagChipColors() = FilterChipDefaults.filterChipColors()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterContent(
    category: CourseFilterCategory,
    selectedFilters: CourseFilterState,
    onFilterChange: (CourseFilterState) -> Unit,
    options: List<CourseFilterOption>,
) {
    if (category == CourseFilterCategory.Department) {
        DepartmentPicker(selectedFilters.departments) { departments ->
            onFilterChange(selectedFilters.copy(departments = departments))
        }
        return
    }
    val selectedIds = selectedFilters.selectedOptionIds(category)
    val selectedCount = selectedIds.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        FilterSheetHeader(
            title = stringResource(category.labelResId),
            selectedCount = selectedCount,
            onReset = { onFilterChange(selectedFilters.withSelection(category, emptyList())) },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option.id in selectedIds

                TagChip(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = {
                        val next = when {
                            category == CourseFilterCategory.Period -> if (isSelected) emptyList() else listOf(option.id)
                            isSelected -> selectedIds - option.id
                            else -> selectedIds + option.id
                        }
                        val isAll = category != CourseFilterCategory.Period && next.size >= options.size
                        onFilterChange(selectedFilters.withSelection(category, if (isAll) emptyList() else next))
                    }
                )
            }
        }
    }
}

/**
 * A filter sheet's title, with how many options are selected, and its Reset button, shared by
 * every course filter sheet so they read as one set.
 */
@Composable
fun FilterSheetHeader(
    title: String,
    selectedCount: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    resetEnabled: Boolean = selectedCount > 0,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title + if (selectedCount > 0) " " + stringResource(R.string.selected_count, selectedCount) else "",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        TextButton(onClick = onReset, enabled = resetEnabled, shapes = ButtonDefaults.shapes()) {
            Text(stringResource(R.string.reset))
        }
    }
}

/** A standard Material 3 Expressive filter chip: a check appears at the start while selected. */
@Composable
private fun TagChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        colors = getTagChipColors()
    )
}

@Preview
@Composable
private fun Preview() {
    Theme {
        CategoryFilterContent(
            category = CourseFilterCategory.Classification,
            selectedFilters = CourseFilterState(
                classifications = listOf("MR", "ME")
            ),
            onFilterChange = {},
            options = listOf(
                CourseFilterOption("BR", "BR"),
                CourseFilterOption("MR", "MR"),
                CourseFilterOption("ME", "ME"),
                CourseFilterOption("HSE", "HSE")
            )
        )
    }
}
