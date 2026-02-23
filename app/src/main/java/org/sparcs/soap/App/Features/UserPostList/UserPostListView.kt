package org.sparcs.soap.App.Features.UserPostList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraPost
import org.sparcs.soap.App.Domain.Models.Ara.AraPostAuthor
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Features.PostList.Components.PostList.PostList
import org.sparcs.soap.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.soap.App.Features.UserPostList.Components.UserPostNavigationBar
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Mocks.mockList
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.BuddyPreviewSupport.Post.PreviewUserPostListViewModel
import org.sparcs.soap.BuddyPreviewSupport.Post.previewAuthor
import org.sparcs.soap.R

@Composable
fun UserPostListView(
    viewModel: UserPostListViewModelProtocol = hiltViewModel(),
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

    Scaffold(
        topBar = {
            UserPostNavigationBar(
                title = user.profile.nickname,
                onClickSearch = { showSearchBar = !showSearchBar },
                isSelected = showSearchBar,
                navController = navController
            )
        },
        modifier = Modifier.analyticsScreen(name = "Ara User Post List")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                if (showSearchBar) {
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

                when (state) {
                    is UserPostListViewModel.ViewState.Loading -> {
                        Column {
                            repeat(15) { PostListSkeletonRow() }
                        }
                    }

                    is UserPostListViewModel.ViewState.Loaded -> {
                        val posts = (state as UserPostListViewModel.ViewState.Loaded).posts
                        PostList(
                            posts = posts,
                            onPostClick = { post ->
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
                            onPostDisappear = { postID -> viewModel.refreshItem(postID) },
                            isRefreshing = isRefreshing
                        )
                    }

                    is UserPostListViewModel.ViewState.Error -> {
                        val message = (state as UserPostListViewModel.ViewState.Error).message
                        ErrorView(
                            icon = Icons.Default.Warning,
                            message = message,
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
        initialState = UserPostListViewModel.ViewState.Error("Something went wrong"),
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