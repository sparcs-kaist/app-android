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
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.PostList.Components.PostList.PostList
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.soap.App.Features.Settings.Components.SettingsViewNavigationBar
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
        }
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
                        onPostDisappear = { postID -> viewModel.refreshItem(postID) },
                        navController = navController
                    )
                }

                AraMyPostViewModel.PostType.BOOKMARK -> {
                    BookmarkPostView(
                        state = state,
                        posts = posts,
                        onRefresh = { coroutineScope.launch { viewModel.fetchInitialPosts() } },
                        onLoadMore = { coroutineScope.launch { viewModel.loadNextPage() } },
                        onPostDisappear = { postID -> viewModel.refreshItem(postID) },
                        navController = navController
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
    onPostDisappear: (Int) -> Unit,
    navController: NavController,
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
                onPostDisappear = onPostDisappear,
                navController = navController
            )

            is AraMyPostViewModel.ViewState.Error -> ErrorView(
                icon = Icons.Default.Warning,
                message = state.message,
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
    navController: NavController,
    onPostDisappear: (Int) -> Unit,
) {
    when (state) {
        is AraMyPostViewModel.ViewState.Loading -> LoadingView()
        is AraMyPostViewModel.ViewState.Loaded -> LoadedView(
            posts = posts,
            onRefresh = onRefresh,
            onLoadMore = onLoadMore,
            onPostDisappear = onPostDisappear,
            navController = navController
        )

        is AraMyPostViewModel.ViewState.Error -> ErrorView(
            icon = Icons.Default.Clear,
            message = state.message,
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
    onPostDisappear: (Int) -> Unit,
    navController: NavController,
) {
    PostList(
        posts = posts,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onPostClick = { post ->
            navController.navigate(Channel.PostView.name + "?postId=${post.id}")
        },
        onPostDisappear = onPostDisappear,
        isRefreshing = false
    )
}