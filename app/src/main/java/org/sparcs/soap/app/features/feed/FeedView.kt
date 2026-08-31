package org.sparcs.soap.app.features.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.features.feed.components.FeedPostRow
import org.sparcs.soap.app.features.feed.components.FeedPostRowSkeleton
import org.sparcs.soap.app.features.feed.components.FeedViewNavigationBar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.feed.PreviewFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel<FeedViewModel>(),
    navController: NavController,
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val loadedInitialPost = rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle
    val listNeedsRefresh = stateHandle?.getStateFlow("listNeedsRefresh", false)?.collectAsState()
    val pullState = rememberPullToRefreshState()

    PullToRefreshHapticHandler(pullState, isRefreshing)

    LaunchedEffect(Unit) {
        if (!loadedInitialPost.value) {
            loadedInitialPost.value = true
            viewModel.fetchInitialData()
        }
    }

    LaunchedEffect(listNeedsRefresh?.value) {
        if (listNeedsRefresh?.value == true) {
            viewModel.fetchInitialData()
            stateHandle["listNeedsRefresh"] = false
        }
    }

    //LoadMore
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItemsCount - 2
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { shouldLoadMore }
            .collect { loadingTriggered ->
                if (loadingTriggered && state is FeedViewModel.ViewState.Loaded && !viewModel.isLoadingMore) {
                    viewModel.loadNextPage()
                }
            }
    }

    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    Scaffold(
        topBar = {
            FeedViewNavigationBar(
                listState = listState,
                navController = navController,
                viewModel = viewModel,
                scrollBehavior = topBarScrollBehavior
            )
        },
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(name = "Feed")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        viewModel.refreshFeed()
                        delay(500)
                        isRefreshing = false
                    }
                },
                state = pullState,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (state) {
                        is FeedViewModel.ViewState.Loading -> {
                            items(3) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FeedPostRowSkeleton()
                                    HorizontalDivider(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }

                        is FeedViewModel.ViewState.Loaded -> {
                            itemsIndexed(
                                items = viewModel.posts,
                                key = { _, post -> post.id }
                            ) { index, post ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    FeedPostRow(
                                        post = post,
                                        viewModel = viewModel,
                                        singleLine = true,
                                        onPostDeleted = { postID ->
                                            scope.launch { viewModel.deletePost(postID) }
                                        },
                                        onComment = {
                                            navController.navigate(Channel.FeedPost.name + "?feedId=${post.id}")
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        ),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    )
                                }
                            }
                        }

                        is FeedViewModel.ViewState.Error -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ErrorView(
                                        error = (state as FeedViewModel.ViewState.Error).error,
                                        onRetry = { scope.launch { viewModel.fetchInitialData() } }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    GlobalAlertDialog(
        isPresented = viewModel.isAlertPresented,
        state = viewModel.alertState,
        onDismiss = { viewModel.isAlertPresented = false }
    )
}

// MARK: - Previews
@Preview(showBackground = true, name = "Loading")
@Composable
private fun PreviewLoading() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Loading,
        posts = FeedPost.mockList()
    )
    Theme { FeedView(viewModel, rememberNavController()) }
}

@Preview(showBackground = true, name = "Loaded")
@Composable
private fun PreviewLoaded() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Loaded(FeedPost.mockList()),
        posts = FeedPost.mockList()
    )
    Theme { FeedView(viewModel, rememberNavController()) }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun PreviewError() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Error(Exception("Error"))
    )
    Theme { FeedView(viewModel, rememberNavController()) }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun PreviewEmpty() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Loaded(emptyList()),
        posts = emptyList()
    )
    Theme { FeedView(viewModel, rememberNavController()) }
}