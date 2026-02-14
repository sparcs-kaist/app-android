package org.sparcs.soap.App.Features.Feed

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import org.sparcs.soap.App.Domain.Enums.Feed.FeedDeletionError
import org.sparcs.soap.App.Domain.Models.Feed.FeedPost
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRow
import org.sparcs.soap.App.Features.Feed.Components.FeedPostRowSkeleton
import org.sparcs.soap.App.Features.Feed.Components.FeedViewNavigationBar
import org.sparcs.soap.App.Features.NavigationBar.AppDownBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Shared.Extensions.PullToRefreshHapticHandler
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.ViewModelMocks.Feed.MockFeedViewModel
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.R

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

    var showAlert by remember { mutableStateOf(false) }
    @StringRes var alertTitle: Int by remember { mutableStateOf(0) }
    @StringRes var alertMessage: Int by remember { mutableStateOf(0) }

    fun showAlert(@StringRes title: Int, @StringRes message: Int) {
        alertTitle = title
        alertMessage = message
        showAlert = true
    }

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle
    val listNeedsRefresh = stateHandle
        ?.getStateFlow("listNeedsRefresh", false)
        ?.collectAsState()

    val deletePost: (String) -> Unit = { postID ->
        coroutineScope.launch {
            try {
                viewModel.deletePost(postID)
                viewModel.fetchInitialData()
            } catch (e: Exception) {
                val message = if (e.isNetworkError()) {
                    R.string.network_connection_error
                } else if (e is FeedDeletionError) {
                    e.errorDescription()
                } else {
                    R.string.unexpected_error_deleting_post
                }

                showAlert(
                    title = R.string.error,
                    message = message
                )
            }
        }
    }
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
                        items(state.posts) { post ->
                            FeedPostRow(
                                post = post,
                                viewModel = viewModel,
                                singleLine = true,
                                onPostDeleted = { postID ->
                                    deletePost(postID)
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
                        message = state.message,
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
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            title = { Text(stringResource(alertTitle)) },
            text = { Text(stringResource(alertMessage)) }
        )
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
