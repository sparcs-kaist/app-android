package org.sparcs.soap.app.features.feed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.feed.FeedPost
import org.sparcs.soap.app.features.feed.components.FeedPostRow
import org.sparcs.soap.app.features.feed.components.FeedPostRowSkeleton
import org.sparcs.soap.app.features.feed.components.FeedViewNavigationBar
import org.sparcs.soap.app.features.feedPost.FeedPostView
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import org.sparcs.soap.app.features.feedPost.FeedPostViewModelProtocol
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.PullToRefreshHapticHandler
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.feed.mockList
import org.sparcs.soap.app.shared.viewModelMocks.feed.MockFeedPostViewModel
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.GlobalAlertDialog
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.feed.PreviewFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel<FeedViewModel>(),
    navController: NavController,
    postViewModel: FeedPostViewModelProtocol? = null,
    split: FeedSplit? = rememberFeedSplit(),
    selection: MutableState<FeedPostSelection> = rememberFeedPostSelection(),
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val loadedInitialPost = rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val currentEntry by navController.currentBackStackEntryAsState()
    val isCurrentFeed = currentEntry?.destination?.route == Channel.Start.name
    val activePostID = selection.value.postID
    val panelVisible = split != null && activePostID != null &&
        selection.value.isPanel
    val stateHandle = navController.currentBackStackEntry?.savedStateHandle

    fun openPost(postID: String) {
        selection.value = FeedPostSelection(postID, isPanel = split != null)
        if (split == null) navController.navigate(Channel.FeedPost.name + "?feedId=$postID")
    }

    BackHandler(enabled = isCurrentFeed && panelVisible) { selection.value = FeedPostSelection() }

    LaunchedEffect(isCurrentFeed, split) {
        if (!isCurrentFeed || activePostID == null) return@LaunchedEffect
        if (!selection.value.isPanel) {
            // Returning with Back ends the active post; a layout transfer marks it as a panel first.
            selection.value = FeedPostSelection()
        } else if (split == null) {
            selection.value = selection.value.copy(isPanel = false)
            navController.navigate(Channel.FeedPost.name + "?feedId=$activePostID")
        } else {
            val index = viewModel.posts.indexOfFirst { it.id == activePostID }
            if (index >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.key == activePostID }) {
                listState.scrollToItem(index)
            }
        }
    }

    LaunchedEffect(viewModel.posts, state, activePostID) {
        if (isCurrentFeed && panelVisible && state is FeedViewModel.ViewState.Loaded &&
            viewModel.posts.none { it.id == activePostID }) {
            selection.value = FeedPostSelection()
        }
    }
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

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Row(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                FeedViewNavigationBar(
                    navController = navController,
                    viewModel = viewModel,
                    scrollBehavior = topBarScrollBehavior
                )
            },
            floatingActionButton = {
                // Shows its label at the top of the feed and shrinks to the icon once scrolled.
                val expanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
                val writeLabel = stringResource(R.string.write)
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.writeFeedButtonTapped()
                        navController.navigate(Channel.FeedPostCompose.name)
                    },
                    expanded = expanded,
                    icon = {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = if (expanded) null else writeLabel
                        )
                    },
                    text = { Text(writeLabel) }
                )
            },
            modifier = (if (panelVisible && split.feedWidth != null) Modifier.width(split.feedWidth)
                else Modifier.weight(1f))
                .testTag("feed-timeline")
                .then(
                    if (panelVisible) Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.End))
                    else Modifier
                )
                .hideTopBarOnScroll(topBarScrollBehavior)
                .analyticsScreen(name = "Feed")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("feed-timeline-content")
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
                            listState.scrollToItem(0)
                            isRefreshing = false
                        }
                    },
                    state = pullState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        // Bottom room so the last post can scroll clear of the Write button.
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
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
                                ) { _, post ->
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
                                                openPost(post.id)
                                            },
                                            onCommentButton = {
                                                if (panelVisible && activePostID == post.id) selection.value = FeedPostSelection()
                                                else openPost(post.id)
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
        if (panelVisible && postViewModel != null) {
            Spacer(Modifier.width(split.gutter))
            Box(
                Modifier.width(split.panelWidth).fillMaxHeight()
                    .testTag("feed-comments-panel")
                    .background(MaterialTheme.colorScheme.surface)
                    .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End + WindowInsetsSides.Bottom))
            ) {
                key(activePostID) {
                    FeedPostView(
                        viewModel = postViewModel,
                        feedViewModel = viewModel,
                        navController = navController,
                        onClosePanel = { selection.value = FeedPostSelection() },
                        initialCommentID = selection.value.commentID,
                    )
                }
                VerticalDivider(Modifier.align(Alignment.CenterStart).offset(x = -split.gutter / 2))
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
    Theme { FeedView(viewModel, rememberNavController(), MockFeedPostViewModel(FeedPostViewModel.ViewState.Loading)) }
}

@Preview(showBackground = true, name = "Loaded")
@Composable
private fun PreviewLoaded() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Loaded(FeedPost.mockList()),
        posts = FeedPost.mockList()
    )
    Theme { FeedView(viewModel, rememberNavController(), MockFeedPostViewModel(FeedPostViewModel.ViewState.Loading)) }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun PreviewError() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Error(Exception("Error"))
    )
    Theme { FeedView(viewModel, rememberNavController(), MockFeedPostViewModel(FeedPostViewModel.ViewState.Loading)) }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun PreviewEmpty() {
    val viewModel = PreviewFeedViewModel(
        initialState = FeedViewModel.ViewState.Loaded(emptyList()),
        posts = emptyList()
    )
    Theme { FeedView(viewModel, rememberNavController(), MockFeedPostViewModel(FeedPostViewModel.ViewState.Loading)) }
}
