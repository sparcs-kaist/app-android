package org.sparcs.soap.app.features.postList.components.marketGrid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.sparcs.soap.app.domain.models.ara.AraMarketListing
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.features.postList.components.postList.EmptyPostsView
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun MarketGrid(
    posts: List<AraPost>?,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (AraPost) -> Unit,
    isRefreshing: Boolean,
    keyword: String? = "",
    contentPadding: PaddingValues = PaddingValues(0.dp),
    gridState: LazyGridState = rememberLazyGridState(),
    isLoadingMore: Boolean = false,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val columns = MarketGridLayout.columnCount(maxWidth.value)
        val padding = contentPadding.withHorizontal(MarketGridLayout.MARGIN.dp)

        when {
            posts == null -> LoadingGrid(
                columns = columns,
                count = MarketGridLayout.placeholderCount(maxWidth.value, maxHeight.value),
                contentPadding = padding
            )

            posts.isEmpty() && keyword.isNullOrEmpty() -> EmptyPostsView(
                onRefresh = onRefresh,
                isRefreshing = isRefreshing,
                contentPadding = contentPadding,
                icon = Icons.Outlined.ShoppingBag
            )

            else -> LoadedGrid(
                posts = posts,
                columns = columns,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onPostClick = onPostClick,
                isRefreshing = isRefreshing,
                contentPadding = padding,
                gridState = gridState,
                isLoadingMore = isLoadingMore
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadedGrid(
    posts: List<AraPost>,
    columns: Int,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (AraPost) -> Unit,
    isRefreshing: Boolean,
    contentPadding: PaddingValues,
    gridState: LazyGridState,
    isLoadingMore: Boolean,
) {
    val pullState = rememberPullToRefreshState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    PullToRefreshHapticHandler(pullState, isRefreshing)

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = gridState.layoutInfo.totalItemsCount
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(MarketGridLayout.SPACING.dp),
            verticalArrangement = Arrangement.spacedBy(MarketGridLayout.ROW_SPACING.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(posts, key = { it.id }) { post ->
                MarketGridCell(
                    post = post,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !post.isHidden) { onPostClick(post) }
                )
            }

            if (isLoadingMore) {
                item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingGrid(columns: Int, count: Int, contentPadding: PaddingValues) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(MarketGridLayout.SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(MarketGridLayout.ROW_SPACING.dp),
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize()
    ) {
        items(count) { MarketGridSkeletonCell() }
    }
}

@Composable
private fun PaddingValues.withHorizontal(horizontal: Dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direction) + horizontal,
        top = calculateTopPadding(),
        end = calculateEndPadding(direction) + horizontal,
        bottom = calculateBottomPadding()
    )
}

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        MarketGrid(posts = null, onRefresh = {}, onLoadMore = {}, onPostClick = {}, isRefreshing = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    Theme {
        MarketGrid(posts = emptyList(), onRefresh = {}, onLoadMore = {}, onPostClick = {}, isRefreshing = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadedPreview() {
    val posts = AraPost.mockList().mapIndexed { index, post ->
        post.copy(marketListing = AraMarketListing(price = (index + 1) * 5_000L))
    }
    Theme {
        MarketGrid(posts = posts, onRefresh = {}, onLoadMore = {}, onPostClick = {}, isRefreshing = false)
    }
}
