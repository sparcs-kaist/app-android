package org.sparcs.Features.Feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.sparcs.Domain.Models.Feed.FeedPost
import org.sparcs.Features.Feed.Components.FeedPostRow
import org.sparcs.Features.Feed.Components.FeedPostRowSkeleton
import org.sparcs.Features.Feed.Components.FeedViewNavigationBar
import org.sparcs.Features.NavigationBar.AppDownBar
import org.sparcs.Features.NavigationBar.Channel
import org.sparcs.Shared.Mocks.mockList
import org.sparcs.Shared.ViewModelMocks.Feed.MockFeedViewModel
import org.sparcs.Shared.Views.ContentViews.ErrorView
import org.sparcs.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState().value
    var isRefreshing by remember { mutableStateOf(false) }
    var loadedInitialPost = rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle
    val listNeedsRefresh = stateHandle
        ?.getStateFlow("listNeedsRefresh", false)
        ?.collectAsState()


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
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (!viewModel.isLoadingMore && lastVisibleIndex != null && lastVisibleIndex >= totalItems - 1) {
                    viewModel.isLoadingMore = true
                    try {
                        coroutineScope.launch {
                            viewModel.loadNextPage()
                        }
                    } finally {
                        viewModel.isLoadingMore = false
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            FeedViewNavigationBar(
                scrollState = scrollState,
                navController = navController
            )
        },
        bottomBar = {
            AppDownBar(
                currentScreen = Channel.Start,
                navController = navController
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    viewModel.fetchInitialData()
                    isRefreshing = false
                }
            }
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
                    LazyColumn(
                        modifier = Modifier
                            .padding(innerPadding),
                        state = listState
                    ) {
                        items(state.posts) { post ->
                            FeedPostRow(
                                post = post,
                                viewModel = viewModel,
                                singleLine = true,
                                onPostDeleted = { postID ->
                                    coroutineScope.launch {
                                        viewModel.deletePost(postID)
                                        viewModel.fetchInitialData()
                                    }
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
                    val message = state.message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = message,
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
}
/* ____________________________________________________________________*/

@Composable
private fun MockView(state: FeedViewModel.ViewState) {
    val mockViewModel = remember { MockFeedViewModel(initialState = state) }
    FeedView(viewModel = mockViewModel, navController = rememberNavController())
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(FeedViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(FeedViewModel.ViewState.Loaded(FeedPost.mockList())) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(FeedViewModel.ViewState.Error("Error Message")) }
}
