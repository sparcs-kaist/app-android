package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.theme.ui.Theme

/**
 * A titled group of results in an outlined card. On the All scope, More switches to
 * [targetScope] to show every result of that kind.
 */
@Composable
fun SearchSection(
    title: String,
    searchScope: SearchScope,
    targetScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )

            if (searchScope != targetScope) {
                TextButton(onClick = { onScopeChange(targetScope) }) {
                    Text(stringResource(R.string.more))
                }
            }
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun CourseSection(
    courses: List<CourseSummary>,
    searchScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    navController: NavController,
    onLoadMore: (() -> Unit)? = null,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = stringResource(R.string.courses),
        searchScope = searchScope,
        targetScope = SearchScope.Courses,
        onScopeChange = onScopeChange
    ) {
        SearchContent(
            results = courses,
            key = { it.id },
            onLoadMore = onLoadMore,
        ) { course ->
            if (isSkeleton) {
                SearchItemSkeleton(overline = true, supportingLines = 2)
            } else {
                CourseCell(
                    course = course,
                    onClick = {
                        navController.navigate(Channel.CourseView.name + "?courseId=${course.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun PostSection(
    posts: List<AraPost>,
    searchScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (AraPost) -> Unit,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = stringResource(R.string.posts),
        searchScope = searchScope,
        targetScope = SearchScope.Posts,
        onScopeChange = onScopeChange
    ) {
        SearchContent(
            results = posts,
            key = { it.id },
            onLoadMore = if (searchScope == SearchScope.Posts) onLoadMore else null
        ) { post ->
            if (isSkeleton) {
                SearchItemSkeleton()
            } else {
                SearchPostItem(post) { onPostClick(post) }
            }
        }
    }
}

@Composable
fun TaxiSection(
    rooms: List<TaxiRoom>,
    searchScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    onTaxiClick: (TaxiRoom) -> Unit,
    onLoadMore: (() -> Unit)? = null,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = stringResource(R.string.rides),
        searchScope = searchScope,
        targetScope = SearchScope.Rides,
        onScopeChange = onScopeChange,
    ) {
        SearchContent(
            results = rooms,
            key = { it.id },
            onLoadMore = onLoadMore
        ) { room ->
            if (isSkeleton) {
                SearchItemSkeleton(leading = true)
            } else {
                SearchTaxiRoomItem(room) { onTaxiClick(room) }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        Column{
            SearchSection(
                title = "Search",
                searchScope = SearchScope.All,
                targetScope = SearchScope.Rides,
                onScopeChange = {},
                content = { CourseCell(CourseSummary.mock()) {} }
            )

            SearchSection(
                title = "Search",
                searchScope = SearchScope.All,
                targetScope = SearchScope.Rides,
                onScopeChange = {},
                content = { SearchTaxiRoomItem(TaxiRoom.mock()) {} }
            )
        }
    }
}