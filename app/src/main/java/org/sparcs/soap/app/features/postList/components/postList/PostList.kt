package org.sparcs.soap.app.features.postList.components.postList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.components.postListRow.PostListRow
import org.sparcs.soap.app.features.postList.components.postListRow.PostListSkeletonRow
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.lightGray0

@Composable
fun PostList(
    posts: List<AraPost>?,
    onRefresh: (() -> Unit),
    onLoadMore: (() -> Unit),
    onPostClick: (AraPost) -> Unit,
    isRefreshing: Boolean,
    keyword: String? = "",
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (posts != null && posts.isEmpty() && keyword == null) {
        //그냥 empty한 경우 (keyword == null)
        ErrorView(
            icon = Icons.Default.Clear,
            defaultMessageResId = R.string.empty_posts_default,
            error = Exception(),
            onRetry = { onRefresh() }//TODO - 번역?
        )
    }
    when {
        posts == null -> {
            LoadingView(contentPadding = contentPadding)
        }

        else -> {
            LoadedView(
                posts = posts,
                onLoadMore = { onLoadMore() },
                onPostClick = onPostClick,
                onRefresh = onRefresh,
                isRefreshing = isRefreshing,
                contentPadding = contentPadding
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadedView(
    posts: List<AraPost>,
    onLoadMore: (() -> Unit),
    onPostClick: (AraPost) -> Unit,
    onRefresh: (() -> Unit),
    isRefreshing: Boolean,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {

    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (!isLoadingMore && lastVisibleIndex != null && lastVisibleIndex >= totalItems - 1) {
                    isLoadingMore = true
                    try {
                        onLoadMore()
                    } finally {
                        isLoadingMore = false
                    }
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState
    ) {
        LazyColumn(state = listState, contentPadding = contentPadding) {
            itemsIndexed(posts) { index, post ->
                PostListRow(
                    post = post,
                    modifier = Modifier.clickable(enabled = !post.isHidden) { onPostClick(post) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
            }

            if (isLoadingMore) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView(contentPadding: PaddingValues = PaddingValues(0.dp)) {
    LazyColumn(contentPadding = contentPadding) {
        repeat(4) {
            item {
                PostListSkeletonRow()
                HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
            }
        }
    }
}


@Composable
@Preview
private fun LoadingPreview() {
    Theme {
        PostList(posts = null, onRefresh = {}, onLoadMore = {}, onPostClick = {}, false)
    }
}

@Composable
@Preview
private fun EmptyPreview() {
    Theme {
        PostList(posts = emptyList(), onRefresh = {}, onLoadMore = {}, onPostClick = {}, false)
    }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme {
        PostList(
            posts = AraPost.mockList(),
            onRefresh = {},
            onLoadMore = {},
            onPostClick = {},
            isRefreshing = false
        )
    }
}
