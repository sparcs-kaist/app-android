package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterProvider
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.theme.ui.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFilterRow(
    modifier: Modifier = Modifier,
    courseFilterState: CourseFilterState,
    onCategoryClick: (CourseFilterCategory) -> Unit,
    onResetFilters: () -> Unit,
    onTimeClick: (() -> Unit)? = null,
    isTimeSelected: Boolean = false,
    showReset: Boolean = !courseFilterState.isEmpty(),
    showLeadingDivider: Boolean = true,
    showPeriod: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Separates these from the search scope chips before them on the search screen.
        if (showLeadingDivider) {
            VerticalDivider(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(20.dp)
            )
        }

        if (showReset) {
            AssistChip(
                onClick = onResetFilters,
                label = { Text(stringResource(R.string.filter_reset)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        CourseFilterCategory.entries.filter { showPeriod || it != CourseFilterCategory.Period }.forEach { category ->
            val selectedOptionIds = courseFilterState.selectedOptionIds(category)
            val isSelected = selectedOptionIds.isNotEmpty()

            val options = CourseFilterProvider.getOptions(category)
            val selectedOptionLabels = if (category == CourseFilterCategory.Department) {
                courseFilterState.departments.map { it.code }
            } else {
                selectedOptionIds.map { id -> options.find { it.id == id }?.label ?: id }
            }

            val baseLabel = stringResource(category.labelResId)
            val labelText = if (isSelected) {
                val displayOptions = selectedOptionLabels.take(2)
                val extraCount = selectedOptionLabels.size - 2
                val optionsText = displayOptions.joinToString(", ")
                if (extraCount > 0) {
                    val othersText = stringResource(R.string.and_n_others, extraCount)
                    "$baseLabel: $optionsText $othersText"
                } else {
                    "$baseLabel: $optionsText"
                }
            } else {
                baseLabel
            }
            MenuFilterChip(isSelected, labelText) { onCategoryClick(category) }
        }

        if (onTimeClick != null) {
            MenuFilterChip(isTimeSelected, stringResource(R.string.lecture_time_filter), onTimeClick)
        }
    }
}

/** Filter chip that opens a sheet of options: a check once something is picked, an arrow always. */
@Composable
private fun MenuFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        modifier = Modifier.padding(end = 8.dp)
    )
}

@Preview
@Composable
private fun Preview() {
    Theme {
        CourseFilterRow(
            courseFilterState = CourseFilterState(
                classifications = listOf("MR", "BR", "BE"),
                departments = listOf(DepartmentOption(9945, "School of Computing", "CS")),
                levels = emptyList(),
                period = "0"
            ),
            onCategoryClick = {},
            onResetFilters = {}
        )
    }
}
