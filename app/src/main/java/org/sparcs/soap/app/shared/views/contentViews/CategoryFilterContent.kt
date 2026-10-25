package org.sparcs.soap.app.shared.views.contentViews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val title = stringResource(category.labelResId)
            val subtitle = if (selectedCount > 0) " " + stringResource(R.string.selected_count, selectedCount) else ""
            Text(
                text = title + subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.reset),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onFilterChange(selectedFilters.withSelection(category, emptyList()))
                }
            )
        }

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

@Composable
private fun TagChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(100.dp),
        border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            borderWidth = 1.dp,
            enabled = true,
            selected = false
        ),
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
