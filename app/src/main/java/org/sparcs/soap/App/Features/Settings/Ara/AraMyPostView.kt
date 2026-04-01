package org.sparcs.soap.App.Features.Settings.Ara

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.PostList.Components.PostList.PostList
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.soap.App.Shared.Views.ContentViews.UnavailableView
import org.sparcs.soap.App.theme.ui.lightGray0
import org.sparcs.soap.R

@Composable
fun AraMyPostView(
    viewModel: AraMyPostViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    var loadedInitialPosts by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val posts by viewModel.posts.collectAsState()

    val searchKeyword by viewModel.searchKeyword.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val type = viewModel.type

    LaunchedEffect(type) {
        if (!loadedInitialPosts) {
            viewModel.bind()
            loadedInitialPosts = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.lastClickedPostId?.let { id ->
                viewModel.refreshItem(id)
                viewModel.lastClickedPostId = null
            }
        }
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = if (type == AraMyPostViewModel.PostType.ALL) stringResource(R.string.my_posts) else stringResource(
                    R.string.bookmarked
                ),
                onDismiss = { navController.popBackStack() },
                isSearchEnabled = type != AraMyPostViewModel.PostType.BOOKMARK,
                onClickSearch = { showSearchBar = !showSearchBar },
                isSelected = showSearchBar
            )
        },
        modifier = Modifier.analyticsScreen("Ara My Post")
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (showSearchBar && type != AraMyPostViewModel.PostType.BOOKMARK) {
                SearchCustomBar(
                    value = searchKeyword,
                    onValueChange = { value ->
                        viewModel.onSearchTextChange(value)
                    },
                    onValueClear = {
                        viewModel.onSearchTextChange("")
                    },
                    placeHolder = stringResource(R.string.search)
                )
            }

            when (type) {
                AraMyPostViewModel.PostType.ALL -> {
                    MyPostView(
                        state = state,
                        posts = posts,
                        searchKeyword = searchKeyword,
                        onRefresh = { coroutineScope.launch { viewModel.fetchInitialPosts() } },
                        onLoadMore = { coroutineScope.launch { viewModel.loadNextPage() } },
                        onPostClick = { postID ->
                            viewModel.lastClickedPostId = postID
                            navController.navigate(Channel.PostView.name + "?postId=${postID}")
                        }
                    )
                }

                AraMyPostViewModel.PostType.BOOKMARK -> {
                    BookmarkPostView(
                        state = state,
                        posts = posts,
                        onRefresh = { coroutineScope.launch { viewModel.fetchInitialPosts() } },
                        onLoadMore = { coroutineScope.launch { viewModel.loadNextPage() } },
                        onPostClick = { postID ->
                            viewModel.lastClickedPostId = postID
                            navController.navigate(Channel.PostView.name + "?postId=${postID}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MyPostView(
    state: AraMyPostViewModel.ViewState,
    posts: List<AraPost>,
    searchKeyword: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (Int) -> Unit,
) {
    if (searchKeyword.isNotEmpty() && posts.isEmpty()) {
        UnavailableView(
            icon = Icons.Rounded.Search,
            title = stringResource(R.string.no_results),
            description = stringResource(
                id = R.string.no_results_for,
                searchKeyword
            )
        )
    } else {
        when (state) {
            is AraMyPostViewModel.ViewState.Loading -> LoadingView()
            is AraMyPostViewModel.ViewState.Loaded -> LoadedView(
                posts = posts,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                onPostClick = { onPostClick(it) }
            )

            is AraMyPostViewModel.ViewState.Error -> ErrorView(
                icon = Icons.Default.Warning,
                error = state.error,
                onRetry = onRefresh
            )
        }
    }
}

@Composable
private fun BookmarkPostView(
    state: AraMyPostViewModel.ViewState,
    posts: List<AraPost>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (Int) -> Unit,
) {
    when (state) {
        is AraMyPostViewModel.ViewState.Loading -> LoadingView()
        is AraMyPostViewModel.ViewState.Loaded -> LoadedView(
            posts = posts,
            onRefresh = onRefresh,
            onLoadMore = onLoadMore,
            onPostClick = { onPostClick(it) }
        )

        is AraMyPostViewModel.ViewState.Error -> ErrorView(
            icon = Icons.Default.Clear,
            error = state.error,
            onRetry = onRefresh
        )
    }
}

@Composable
private fun LoadingView() {
    Column {
        repeat(15) {
            PostListSkeletonRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
        }
    }
}

@Composable
private fun LoadedView(
    posts: List<AraPost>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onPostClick: (Int) -> Unit,
) {
    if (posts.isEmpty()) {
        UnavailableView(
            icon = Icons.Default.Clear,
            title = stringResource(R.string.no_posts),
            description = ""
        )
    } else {
        PostList(
            posts = posts,
            onRefresh = onRefresh,
            onLoadMore = onLoadMore,
            onPostClick = { post ->
                onPostClick(post.id)
            },
            isRefreshing = false
        )
    }
}