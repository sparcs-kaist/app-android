package com.example.soap.Features.UserPostList

import android.net.Uri
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostList.PostList
import com.example.soap.Features.PostList.Components.PostListRow.PostListSkeletonRow
import com.example.soap.Features.UserPostList.Components.UserPostNavigationBar
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun UserPostListView(
    viewModel: UserPostListViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val user = viewModel.user
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.bind()
        viewModel.fetchInitialPosts()
    }

    Scaffold(
        topBar = {
            UserPostNavigationBar(
                title = user.profile.nickname,
                navController = navController
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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
                            val json = Uri.encode(Gson().toJson(post))
                            navController.navigate(Channel.PostView.name + "?post_json=$json")
                        },
                        onRefresh = {
                            coroutineScope.launch { viewModel.fetchInitialPosts() }
                        },
                        onLoadMore = {
                            coroutineScope.launch { viewModel.loadNextPage() }
                        },
                        onPostDisappear = { postID -> viewModel.refreshItem(postID)},
                        isRefreshing = false
                    )
                }

                is UserPostListViewModel.ViewState.Error -> {
                    val message = (state as UserPostListViewModel.ViewState.Error).message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = message,
                        onRetry = {
                            coroutineScope.launch { viewModel.fetchInitialPosts() }
                        }
                    )
                }
            }
        }
    }
}



@Composable
@Preview
private fun Preview(){
    Theme {
        UserPostListView(navController = rememberNavController())
    }
}
