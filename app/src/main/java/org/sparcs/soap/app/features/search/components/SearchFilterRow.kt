package org.sparcs.soap.app.features.search.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun SearchFilterRow(
    searchScope: SearchScope,
    courseFilterState: CourseFilterState,
    onScopeChange: (SearchScope) -> Unit,
    onCategoryClick: (CourseFilterCategory) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(SearchScope.entries) { scope ->
                val selected = searchScope == scope
                FilterChip(
                    selected = selected,
                    onClick = { onScopeChange(scope) },
                    label = { Text(stringResource(scope.labelRes)) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            item {
                AnimatedVisibility(
                    visible = searchScope == SearchScope.Courses,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    CourseFilterRow(
                        courseFilterState = courseFilterState,
                        onCategoryClick = onCategoryClick,
                        onResetFilters = onResetFilters
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        SearchFilterRow(
            searchScope = SearchScope.Courses,
            courseFilterState = CourseFilterState(
                classifications = listOf("MR"),
                departments = listOf(DepartmentOption(9945, "School of Computing", "CS")),
                levels = emptyList(),
                period = "0"
            ),
            onScopeChange = {},
            onCategoryClick = {},
            onResetFilters = {}
        )
    }
}
