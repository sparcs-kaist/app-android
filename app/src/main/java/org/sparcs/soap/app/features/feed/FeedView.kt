package org.sparcs.soap.app.features.feed

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.features.feed.components.FeedPostRow
import org.sparcs.soap.app.features.feed.components.FeedPostRowSkeleton
import org.sparcs.soap.app.features.feed.components.FeedViewNavigationBar
import org.sparcs.soap.app.features.navigationBar.AppDownBar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
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

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle
    val listNeedsRefresh = stateHandle
        ?.getStateFlow("listNeedsRefresh", false)
        ?.collectAsState()
    val pullState = rememberPullToRefreshState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
            .analyticsScreen(name = "Feed")
    ) { innerPadding ->
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
            modifier = Modifier.padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLandscape) {
                    FeedLandscapeLayout(
                        state = state,
                        viewModel = viewModel,
                        navController = navController,
                        listState = listState,
                        scope = scope
                    )
                } else {
                    FeedPortraitLayout(
                        state = state,
                        viewModel = viewModel,
                        navController = navController,
                        listState = listState,
                        scope = scope
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

@Composable
private fun FeedLandscapeLayout(
    state: FeedViewModel.ViewState,
    viewModel: FeedViewModelProtocol,
    navController: NavController,
    listState: LazyListState,
    scope: CoroutineScope
) {
    FeedPortraitLayout(state, viewModel, navController, listState, scope)
}

@Composable
private fun FeedPortraitLayout(
    state: FeedViewModel.ViewState,
    viewModel: FeedViewModelProtocol,
    navController: NavController,
    listState: LazyListState,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
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
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(
                        items = viewModel.posts,
                        key = { _, post -> post.id }
                    ) { index, post ->
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

                        HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }

            is FeedViewModel.ViewState.Error -> {
                ErrorView(
                    error = (state as FeedViewModel.ViewState.Error).error,
                    onRetry = {
                        scope.launch { viewModel.fetchInitialData() }
                    },
                )
            }
        }
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

@Preview(showBackground = true, name = "Loaded Landscape", widthDp = 840, heightDp = 480)
@Composable
private fun PreviewLoadedLandscape() {
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
