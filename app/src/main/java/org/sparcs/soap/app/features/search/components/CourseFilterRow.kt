package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.shared.views.contentViews.getTagChipColors
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun CourseFilterRow(
    courseFilterState: CourseFilterState,
    onCategoryClick: (CourseFilterCategory) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
    showLeadingDivider: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLeadingDivider) {
            Spacer(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(1.dp)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        if (!courseFilterState.isEmpty()) {
            FilterChip(
                selected = false,
                onClick = onResetFilters,
                label = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.filter_reset),
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(100.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.Transparent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    enabled = true,
                    selected = false
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        CourseFilterCategory.entries.forEach { category ->
            val isSelected = when (category) {
                CourseFilterCategory.Classification -> courseFilterState.classifications.isNotEmpty()
                CourseFilterCategory.Department -> courseFilterState.departments.isNotEmpty()
                CourseFilterCategory.Level -> courseFilterState.levels.isNotEmpty()
                CourseFilterCategory.Period -> courseFilterState.period != null
            }

            FilterChip(
                selected = isSelected,
                onClick = { onCategoryClick(category) },
                label = {
                    Text(
                        text = stringResource(category.labelResId),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(100.dp),
                colors = getTagChipColors(),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        CourseFilterRow(
            courseFilterState = CourseFilterState(
                classifications = listOf("MR"),
                departments = listOf("9945"),
                levels = emptyList(),
                period = "0"
            ),
            onCategoryClick = {},
            onResetFilters = {}
        )
    }
}
