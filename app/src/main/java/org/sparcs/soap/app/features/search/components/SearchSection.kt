package org.sparcs.soap.app.features.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.SearchScope
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.otl.CourseSummary
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.postList.components.postListRow.PostListRow
import org.sparcs.soap.app.features.postList.components.postListRow.PostListSkeletonRow
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomCell
import org.sparcs.soap.app.shared.views.taxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun SearchSection(
    title: String,
    searchScope: SearchScope,
    targetScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            if (searchScope != targetScope) {
                IconButton(
                    onClick = { onScopeChange(targetScope) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding)
        ) {
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
            onLoadMore = onLoadMore,
        ) { course ->
            if (isSkeleton) {
                CourseSkeletonCell()
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
    navController: NavController,
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
            onLoadMore = if (searchScope == SearchScope.Posts) onLoadMore else null
        ) { post ->
            if (isSkeleton) {
                PostListSkeletonRow()
            } else {
                PostListRow(
                    post = post,
                    modifier = Modifier.clickable(enabled = !post.isHidden) {
                        navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                    }
                )
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
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        SearchContent(
            results = rooms,
            onLoadMore = onLoadMore
        ) { room ->
            if (isSkeleton) {
                TaxiRoomSkeletonCell(isSearch = true)
            } else {
                TaxiRoomCell(room = room, isSearch = true) {
                    onTaxiClick(room)
                }
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
                content = { TaxiRoomCell(TaxiRoom.mock(), isSearch = true) }
            )
        }
    }
}