package org.sparcs.soap.app.shared.views.contentViews

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterOption
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterContent(
    category: CourseFilterCategory,
    selectedFilters: CourseFilterState,
    onFilterChange: (CourseFilterState) -> Unit,
    options: List<CourseFilterOption>,
) {
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
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(category.labelResId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAllSelected = when (category) {
                CourseFilterCategory.Classification -> selectedFilters.classifications.isEmpty()
                CourseFilterCategory.Department -> selectedFilters.departments.isEmpty()
                CourseFilterCategory.Level -> selectedFilters.levels.isEmpty()
                CourseFilterCategory.Period -> selectedFilters.period == null
            }

            TagChip(
                label = stringResource(R.string.filter_all),
                isSelected = isAllSelected,
                onClick = {
                    val nextState = when (category) {
                        CourseFilterCategory.Classification -> selectedFilters.copy(classifications = emptyList())
                        CourseFilterCategory.Department -> selectedFilters.copy(departments = emptyList())
                        CourseFilterCategory.Level -> selectedFilters.copy(levels = emptyList())
                        CourseFilterCategory.Period -> selectedFilters.copy(period = null)
                    }
                    onFilterChange(nextState)
                }
            )

            options.forEach { option ->
                val isSelected = when (category) {
                    CourseFilterCategory.Classification ->
                        selectedFilters.classifications.contains(option.id)

                    CourseFilterCategory.Department -> selectedFilters.departments.contains(option.id)
                    CourseFilterCategory.Level -> selectedFilters.levels.contains(option.id)
                    CourseFilterCategory.Period -> selectedFilters.period == option.id
                }

                TagChip(
                    label = option.label,
                    isSelected = isSelected,
                    onClick = {
                        val nextState = when (category) {
                            CourseFilterCategory.Classification -> {
                                val next =
                                    if (isSelected) selectedFilters.classifications - option.id
                                    else selectedFilters.classifications + option.id
                                if (next.size >= options.size || next.isEmpty()) {
                                    selectedFilters.copy(classifications = emptyList())
                                } else {
                                    selectedFilters.copy(classifications = next)
                                }
                            }

                            CourseFilterCategory.Department -> {
                                val next =
                                    if (isSelected) selectedFilters.departments - option.id
                                    else selectedFilters.departments + option.id
                                if (next.size >= options.size || next.isEmpty()) {
                                    selectedFilters.copy(departments = emptyList())
                                } else {
                                    selectedFilters.copy(departments = next)
                                }
                            }

                            CourseFilterCategory.Level -> {
                                val next =
                                    if (isSelected) selectedFilters.levels - option.id
                                    else selectedFilters.levels + option.id
                                if (next.size >= options.size || next.isEmpty()) {
                                    selectedFilters.copy(levels = emptyList())
                                } else {
                                    selectedFilters.copy(levels = next)
                                }
                            }

                            CourseFilterCategory.Period -> {
                                val next = if (isSelected) null else option.id
                                selectedFilters.copy(period = next)
                            }
                        }
                        onFilterChange(nextState)
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
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
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

@Composable
fun getTagChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Color.Transparent,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Preview
@Composable
private fun Preview() {
    Theme {
        CategoryFilterContent(
            category = CourseFilterCategory.Department,
            selectedFilters = CourseFilterState(
                departments = listOf("9945", "833")
            ),
            onFilterChange = {},
            options = listOf(
                CourseFilterOption("9945", "CS"),
                CourseFilterOption("833", "MAS"),
                CourseFilterOption("623", "PH"),
                CourseFilterOption("620", "CH")
            )
        )
    }
}
