package org.sparcs.soap.app.features.postList.components.postList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.components.postListRow.PostListRow
import org.sparcs.soap.app.features.postList.components.postListRow.PostListSkeletonRow
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Pull-to-refresh list of posts that loads more near the end. [rowPadding] insets each row's
 * content while its tap highlight spans the full width; [isLoadingMore] shows a spinner at the end.
 */
@Composable
fun PostList(
    posts: List<AraPost>?,
    onRefresh: (() -> Unit),
    onLoadMore: (() -> Unit),
    onPostClick: (AraPost) -> Unit,
    isRefreshing: Boolean,
    keyword: String? = "",
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = rememberLazyListState(),
    rowPadding: PaddingValues = PaddingValues(0.dp),
    isLoadingMore: Boolean = false,
) {
    when {
        posts == null -> LoadingView(contentPadding = contentPadding, rowPadding = rowPadding)

        // An empty board; an empty search result is handled by the caller.
        posts.isEmpty() && keyword.isNullOrEmpty() -> EmptyPostsView(
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            contentPadding = contentPadding
        )

        else -> LoadedView(
            posts = posts,
            onLoadMore = onLoadMore,
            onPostClick = onPostClick,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
            contentPadding = contentPadding,
            listState = listState,
            rowPadding = rowPadding,
            isLoadingMore = isLoadingMore
        )
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
    contentPadding: PaddingValues,
    listState: LazyListState,
    rowPadding: PaddingValues,
    isLoadingMore: Boolean,
) {
    val pullState = rememberPullToRefreshState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    PullToRefreshHapticHandler(pullState, isRefreshing)

    // Ask for the next page once the last row is visible; the view model ignores repeats while
    // a page is loading.
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= totalItems - 1) {
                    currentOnLoadMore()
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState
    ) {
        LazyColumn(state = listState, contentPadding = contentPadding) {
            items(posts, key = { it.id }) { post ->
                PostListRow(
                    post = post,
                    modifier = Modifier
                        .clickable(enabled = !post.isHidden) { onPostClick(post) }
                        .padding(rowPadding)
                )
                PostDivider(rowPadding)
            }

            if (isLoadingMore) {
                item(key = "loading") {
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
private fun LoadingView(contentPadding: PaddingValues, rowPadding: PaddingValues) {
    LazyColumn(contentPadding = contentPadding, userScrollEnabled = false) {
        items(15) {
            PostListSkeletonRow(it, Modifier.padding(rowPadding))
            PostDivider(rowPadding)
        }
    }
}

/** An empty board, still pullable to refresh: the message sits in a one-item list filling the screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyPostsView(
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    contentPadding: PaddingValues,
) {
    val pullState = rememberPullToRefreshState()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullState
    ) {
        LazyColumn(contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
            item(key = "empty") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Article,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        stringResource(R.string.empty_posts_default),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** Hairline between posts: the default divider color at half strength, so it stays light. */
@Composable
private fun PostDivider(rowPadding: PaddingValues) {
    HorizontalDivider(
        modifier = Modifier.padding(rowPadding.horizontalOnly()),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun PaddingValues.horizontalOnly(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(start = calculateStartPadding(direction), end = calculateEndPadding(direction))
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
