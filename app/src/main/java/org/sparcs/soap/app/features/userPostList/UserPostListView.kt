package org.sparcs.soap.app.features.userPostList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.AraPostAuthor
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.postList.components.postList.PostList
import org.sparcs.soap.app.features.postList.components.postListRow.PostListSkeletonRow
import org.sparcs.soap.app.features.userPostList.components.UserPostNavigationBar
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.landscapeHideOnScrollBehavior
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.shared.views.contentViews.SearchCustomBar
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewUserPostListViewModel
import org.sparcs.soap.buddyPreviewSupport.post.previewAuthor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostListView(
    viewModel: UserPostListViewModelProtocol = hiltViewModel<UserPostListViewModel>(),
    navController: NavController,
) {
    val user = viewModel.user
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.bind()
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

    val topBarScrollBehavior = landscapeHideOnScrollBehavior()

    Scaffold(
        topBar = {
            UserPostNavigationBar(
                title = user.profile.nickname,
                onClickSearch = { showSearchBar = !showSearchBar },
                isSelected = showSearchBar,
                navController = navController,
                scrollBehavior = topBarScrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(name = "Ara User Post List")
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column {
                if (showSearchBar) {
                    Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp)) {
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
                }

                when (state) {
                    is UserPostListViewModel.ViewState.Loading -> {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            repeat(15) { PostListSkeletonRow() }
                        }
                    }

                    is UserPostListViewModel.ViewState.Loaded -> {
                        val posts = (state as UserPostListViewModel.ViewState.Loaded).posts
                        PostList(
                            posts = posts,
                            onPostClick = { post ->
                                viewModel.lastClickedPostId = post.id
                                navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                            },
                            onRefresh = {
                                isRefreshing = true
                                coroutineScope.launch {
                                    viewModel.fetchInitialPosts()
                                    delay(500)
                                    isRefreshing = false
                                }
                            },
                            onLoadMore = {
                                coroutineScope.launch { viewModel.loadNextPage() }
                            },
                            isRefreshing = isRefreshing,
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            )
                        )
                    }

                    is UserPostListViewModel.ViewState.Error -> {
                        val error = (state as UserPostListViewModel.ViewState.Error).error
                        ErrorView(
                            error = error,
                            onRetry = {
                                coroutineScope.launch { viewModel.fetchInitialPosts() }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Mark: Preview
@Preview(name = "Loading State", showBackground = true)
@Composable
private fun PreviewUserPostListLoading() {
    val viewModel = PreviewUserPostListViewModel(
        initialState = UserPostListViewModel.ViewState.Loading,
        user = AraPostAuthor.previewAuthor
    )
    Theme {
        UserPostListView(viewModel = viewModel, rememberNavController())
    }
}

@Preview(name = "Loaded State", showBackground = true)
@Composable
private fun PreviewUserPostListLoaded() {
    val viewModel = PreviewUserPostListViewModel(
        initialState = UserPostListViewModel.ViewState.Loaded(AraPost.mockList()),
        user = AraPostAuthor.previewAuthor,
        initialPosts = AraPost.mockList()
    )
    Theme {
        UserPostListView(viewModel = viewModel, rememberNavController())
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun PreviewUserPostListError() {
    val viewModel = PreviewUserPostListViewModel(
        initialState = UserPostListViewModel.ViewState.Error(Exception()),
        user = AraPostAuthor.previewAuthor
    )
    Theme {
        UserPostListView(viewModel = viewModel, rememberNavController())
    }
}

@Preview(name = "Empty Search", showBackground = true)
@Composable
private fun PreviewUserPostListEmptySearch() {
    val viewModel = PreviewUserPostListViewModel(
        initialState = UserPostListViewModel.ViewState.Loaded(emptyList()),
        user = AraPostAuthor.previewAuthor,
        initialPosts = emptyList(),
        initialSearchKeyword = "no results"
    )
    Theme {
        UserPostListView(viewModel = viewModel, rememberNavController())
    }
}