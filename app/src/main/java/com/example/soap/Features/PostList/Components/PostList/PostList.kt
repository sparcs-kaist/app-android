package com.example.soap.Features.PostList.Components.PostList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.Features.PostList.Components.PostListRow.PostListSkeletonRow
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.lightGray0
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PostList(
    posts: List<AraPost>?,
    onRefresh: ( () -> Unit),
    onLoadMore: ( () -> Unit),
    onPostClick: (AraPost) -> Unit,
    onPostDisappear: (Int) -> Unit,
    isRefreshing: Boolean
) {
    if (posts != null && posts.isEmpty()) {
        ErrorView(
            icon = Icons.Default.Clear,
            errorMessage = "Nothing Here Yet\nIt looks like there are no posts on this page right now.",
            onRetry = { onRefresh() }
        )
    }
    when {
        posts == null -> {
            LoadingView()
        }

        else -> {
            LoadedView(
                posts = posts,
                onLoadMore = { onLoadMore() },
                onPostClick = onPostClick,
                onRefresh = onRefresh,
                onPostDisappear = onPostDisappear,
                isRefreshing = isRefreshing
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadedView(
    posts: List<AraPost>,
    onLoadMore: ( () -> Unit),
    onPostClick: (AraPost) -> Unit,
    onRefresh: ( () -> Unit),
    onPostDisappear: (Int) -> Unit,
    isRefreshing: Boolean
) {

    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
        onRefresh = onRefresh
    ) {
        LazyColumn(state = listState) {
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
private fun LoadingView() {
    LazyColumn {
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
private fun LoadingPreview(){
    Theme {
        PostList(posts = null, onRefresh = {}, onLoadMore = {}, onPostClick = {}, onPostDisappear = {}, false)
    }
}

@Composable
@Preview
private fun EmptyPreview(){
    Theme {
        PostList(posts = emptyList(), onRefresh = {}, onLoadMore = {},onPostClick = {}, onPostDisappear = {}, false)
    }
}

@Composable
@Preview
private fun LoadedPreview(){
    Theme {
        PostList(posts = AraPost.mockList(), onRefresh = {}, onLoadMore = {}, onPostClick = {}, onPostDisappear = {}, isRefreshing= false)
    }
}
