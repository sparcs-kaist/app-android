package org.sparcs.soap.App.Features.Feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRow
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRowSkeleton
import org.sparcs.soap.App.Features.Feed.Components.FeedViewNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.AppDownBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.Feed.PreviewFeedViewModel
import org.sparcs.soap.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var loadedInitialPost = rememberSaveable { mutableStateOf(false) }

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
                        itemsIndexed(viewModel.posts) { index, post ->
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
                            LaunchedEffect(listState) {
                                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                                    .distinctUntilChanged()
                                    .collect { lastVisibleIndex ->
                                        val totalItems = listState.layoutInfo.totalItemsCount
                                        if (!viewModel.isLoadingMore && lastVisibleIndex != null && lastVisibleIndex >= totalItems - 1) {
                                            viewModel.loadNextPage()
                                        }
                                    }
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                is FeedViewModel.ViewState.Error -> {
                    ErrorView(
                        icon = Icons.Default.Warning,
                        message = (state as FeedViewModel.ViewState.Error).message,
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
    if (viewModel.isAlertPresented) {
        AlertDialog(
            onDismissRequest = { viewModel.isAlertPresented = false },
            confirmButton = {
                TextButton(onClick = { viewModel.isAlertPresented = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = {
                viewModel.alertState?.titleResId?.let { Text(stringResource(it)) }
            },
            text = {
                viewModel.alertState?.let { state ->
                    Text(
                        state.message ?: stringResource(
                            state.messageResId ?: R.string.unexpected_error
                        )
                    )
                }
            }
        )
    }
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
        initialState = FeedViewModel.ViewState.Error("Something went wrong")
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