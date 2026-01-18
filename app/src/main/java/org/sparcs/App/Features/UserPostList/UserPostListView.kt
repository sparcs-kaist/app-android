package org.sparcs.App.Features.UserPostList

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
import kotlinx.coroutines.launch
import org.sparcs.App.Domain.Models.Ara.AraPost
import org.sparcs.App.Features.NavigationBar.Channel
import org.sparcs.App.Features.PostList.Components.PostList.PostList
import org.sparcs.App.Features.PostList.Components.PostListRow.PostListSkeletonRow
import org.sparcs.App.Features.UserPostList.Components.UserPostNavigationBar
import org.sparcs.App.Shared.Mocks.mockList
import org.sparcs.App.Shared.ViewModelMocks.Ara.MockUserPostListViewModel
import org.sparcs.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.App.Shared.Views.ContentViews.SearchCustomBar
import org.sparcs.App.theme.ui.Theme
import org.sparcs.R

@Composable
fun UserPostListView(
    viewModel: UserPostListViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val user = viewModel.user
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

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
        }
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
                                coroutineScope.launch { viewModel.fetchInitialPosts() }
                            },
                            onLoadMore = {
                                coroutineScope.launch { viewModel.loadNextPage() }
                            },
                            onPostDisappear = { postID -> viewModel.refreshItem(postID) },
                            isRefreshing = false
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

/* ____________________________________________________________________*/

@Composable
private fun MockView(state: UserPostListViewModel.ViewState) {
    val mockViewModel = remember { MockUserPostListViewModel(initialState = state) }
    UserPostListView(viewModel = mockViewModel, navController = rememberNavController())
}

@Composable
@Preview
private fun LoadingPreview() {
    Theme { MockView(UserPostListViewModel.ViewState.Loading) }
}

@Composable
@Preview
private fun LoadedPreview() {
    Theme { MockView(UserPostListViewModel.ViewState.Loaded(AraPost.mockList())) }
}

@Composable
@Preview
private fun ErrorPreview() {
    Theme { MockView(UserPostListViewModel.ViewState.Error("Error Message")) }
}