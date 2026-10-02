package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CourseFilterCategory
import org.sparcs.soap.app.domain.models.otl.CourseFilterState
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.features.search.components.CourseFilterRow
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.views.contentViews.SearchCustomBar
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureSearchField(
    searchText: String,
    filter: CourseFilterState,
    time: LectureTimeFilter,
    onSearchTextChange: (String) -> Unit,
    onCategoryClick: (CourseFilterCategory) -> Unit,
    onTimeClick: () -> Unit,
    onReset: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {
    val hasAnyFilterSelected = !filter.isEmpty() || !time.isEmpty

    Card(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glassBorder(shape = RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(start = 8.dp)) {
            Box(Modifier.padding(end = 8.dp)) {
                SearchCustomBar(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    onValueClear = { onSearchTextChange("") },
                    placeHolder = stringResource(R.string.search_by_course),
                    onFocusChange = onFocusChange,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CourseFilterRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                courseFilterState = filter,
                onCategoryClick = onCategoryClick,
                onResetFilters = onReset,
                onTimeClick = onTimeClick,
                isTimeSelected = !time.isEmpty,
                showReset = hasAnyFilterSelected,
                showLeadingDivider = true,
                showPeriod = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFieldPreview() {
    Theme {
        LectureSearchField(
            searchText = "",
            filter = CourseFilterState(),
            time = LectureTimeFilter(),
            onSearchTextChange = {},
            onCategoryClick = {},
            onTimeClick = {},
            onReset = {},
            onFocusChange = {}
        )
    }
}
