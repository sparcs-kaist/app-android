package org.sparcs.soap.App.Features.Search.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.OTL.SearchCourse
import org.sparcs.soap.App.Domain.Models.SearchScope
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListRow
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.TaxiRoomCell
import org.sparcs.soap.App.Shared.Views.TaxiRoomCell.TaxiRoomSkeletonCell
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

@Composable
fun SearchSection(
    title: String,
    searchScope: SearchScope,
    targetScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    content: @Composable () -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
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
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun CourseSection(
    cours: List<SearchCourse>,
    searchScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    navController: NavController,
    onLoadMore: (suspend () -> Unit)? = null,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = stringResource(R.string.courses),
        searchScope = searchScope,
        targetScope = SearchScope.Courses,
        onScopeChange = onScopeChange
    ) {
        SearchContent(
            results = cours,
            onLoadMore = onLoadMore,
        ) { course ->
            if (isSkeleton) {
                CourseSkeletonCell()
            } else {
                CourseCell(
                    searchCourse = course,
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
    onLoadMore: suspend () -> Unit,
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
    onLoadMore: (suspend () -> Unit)? = null,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = stringResource(R.string.rides),
        searchScope = searchScope,
        targetScope = SearchScope.Rides,
        onScopeChange = onScopeChange
    ) {
        SearchContent(
            results = rooms,
            onLoadMore = onLoadMore
        ) { room ->
            if (isSkeleton) {
                TaxiRoomSkeletonCell()
            } else {
                TaxiRoomCell(room = room) {
                    onTaxiClick(room)
                }
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        SearchSection(
            title = "Search",
            searchScope = SearchScope.All,
            targetScope = SearchScope.Rides,
            onScopeChange = {},
            content = { CourseCell(SearchCourse.mock(), {}) }
        )
    }
}