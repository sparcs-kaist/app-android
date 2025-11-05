package com.example.soap.Features.Search.Components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.OTL.Course
import com.example.soap.Domain.Models.SearchScope
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.Features.PostList.Components.PostListRow.PostListSkeletonRow
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Views.TaxiRoomCell.TaxiRoomCell
import com.example.soap.Shared.Views.TaxiRoomCell.TaxiRoomSkeletonCell
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson

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
                        painter = painterResource(R.drawable.arrow_forward_ios),
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
    courses: List<Course>,
    searchScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    navController: NavController,
    onLoadMore: (suspend () -> Unit)? = null,
    isSkeleton: Boolean,
) {
    SearchSection(
        title = "Courses",
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
                        val json = Uri.encode(Gson().toJson(course))
                        navController.navigate(Channel.CourseView.name + "?course_json=$json")
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
        title = "Posts",
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
                        val json = Uri.encode(Gson().toJson(post))
                        navController.navigate(Channel.PostView.name + "?post_json=$json")
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
        title = "Rides",
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
            content = { CourseCell(Course.mock(), {}) }
        )
    }
}