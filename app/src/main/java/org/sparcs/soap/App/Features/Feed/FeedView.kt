package org.sparcs.soap.App.Features.Feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRow
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRowSkeleton
import org.sparcs.soap.App.Features.Feed.Components.FeedViewNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.AppDownBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.Feed.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.GlobalAlertDialog
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val loadedInitialPost = rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle
    val listNeedsRefresh = stateHandle
        ?.getStateFlow("listNeedsRefresh", false)
        ?.collectAsState()
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
                if (loadingTriggered &&
                    state is FeedViewModel.ViewState.Loaded &&
                    !viewModel.isLoadingMore
                ) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        topBar = {
            FeedViewNavigationBar(
                scrollState = scrollState,
                navController = navController,
                viewModel = viewModel
            )
        },
        bottomBar = {
            AppDownBar(
                currentScreen = Channel.Start,
                navController = navController
            )
        },
        modifier = Modifier
            .analyticsScreen(name = "Feed"),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.refreshFeed()
                    delay(500)
                    isRefreshing = false
                }
            },
            state = pullState,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (state) {
                is FeedViewModel.ViewState.Loading -> {
                    Column {
                        repeat(3) {
                            FeedPostRowSkeleton()
                            HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                is FeedViewModel.ViewState.Loaded -> {
                    LazyColumn(state = listState) {
                        itemsIndexed(
                            items = viewModel.posts,
                            key = { _, post -> post.id }
                        ) { index, post ->
                            FeedPostRow(
                                post = post,
                                viewModel = viewModel,
                                singleLine = true,
                                onPostDeleted = { postID ->
                                    coroutineScope.launch { viewModel.deletePost(postID) }
                                },
                                onComment = {
                                    navController.navigate(Channel.FeedPost.name + "?feedId=${post.id}")
                                }
                            )
                            HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                is FeedViewModel.ViewState.Error -> {
                    ErrorView(
                        icon = Icons.Default.Warning,
                        error = (state as FeedViewModel.ViewState.Error).error,
                        onRetry = {
                            coroutineScope.launch {
                                viewModel.fetchInitialData()
                            }
                        },
                    )
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