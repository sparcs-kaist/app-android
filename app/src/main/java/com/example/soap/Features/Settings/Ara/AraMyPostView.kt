package com.example.soap.Features.Settings.Ara

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostList.PostList
import com.example.soap.Features.PostList.Components.PostListRow.PostListSkeletonRow
import com.example.soap.Features.Settings.Components.SettingsViewNavigationBar
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.lightGray0
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun AraMyPostView(
    viewModel: AraMyPostViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    var loadedInitialPosts by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val searchKeyword by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val backStackEntry = navController.currentBackStackEntry!!
    val type = viewModel.type

    LaunchedEffect(Unit) {
        val json = Gson().toJson(type)
        backStackEntry.savedStateHandle["type_json"] = json
    }

    LaunchedEffect(type) {
        if (!loadedInitialPosts) {
            viewModel.bind()
            loadedInitialPosts = true
        }
    }

    Scaffold(
        topBar = {
            SettingsViewNavigationBar(
                title = if (type == AraMyPostViewModel.PostType.ALL) "My Posts" else "Bookmarked",
                onDismiss = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            when (type) {
                AraMyPostViewModel.PostType.ALL -> {
                    MyPostView(
                        state = state,
                        posts = posts,
                        searchKeyword = searchKeyword,
                        onRefresh = { coroutineScope.launch { viewModel.fetchInitialPosts() } },
                        onLoadMore = { coroutineScope.launch { viewModel.loadNextPage() } },
                        navController = navController
                    //    onPostDisappear = { postId -> viewModel.refreshItem(postId) }
                    )
                }

                AraMyPostViewModel.PostType.BOOKMARK -> {
                    BookmarkPostView(
                        state = state,
                        posts = posts,
                        onRefresh = { coroutineScope.launch { viewModel.fetchInitialPosts() } },
                        onLoadMore = { coroutineScope.launch { viewModel.loadNextPage() } },
                        navController = navController
                        //onPostDisappear = { postId -> viewModel.refreshItem(postId) }
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
    navController: NavController
  //  onPostDisappear: (Int) -> Unit
) {
    if (searchKeyword.isNotEmpty() && posts.isEmpty()) {
        Text("No results for \"$searchKeyword\"")
    } else {
        when (state) {
            is AraMyPostViewModel.ViewState.Loading -> LoadingView()
            is AraMyPostViewModel.ViewState.Loaded -> LoadedView(
                posts = posts,
                onRefresh = onRefresh,
                onLoadMore = onLoadMore,
                navController = navController
            )
            is AraMyPostViewModel.ViewState.Error -> Text("Error: ${state.message}")
        }
    }
}

@Composable
private fun BookmarkPostView(
    state: AraMyPostViewModel.ViewState,
    posts: List<AraPost>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    navController: NavController
   // onPostDisappear: (Int) -> Unit
) {
    when (state) {
        is AraMyPostViewModel.ViewState.Loading -> LoadingView()
        is AraMyPostViewModel.ViewState.Loaded -> LoadedView(
            posts = posts,
            onRefresh = onRefresh,
            onLoadMore = onLoadMore,
            navController = navController
        )
        is AraMyPostViewModel.ViewState.Error -> ErrorView(
            icon = Icons.Default.Clear,
            errorMessage = state.message,
            onRetry = onRefresh
        )
    }
}

@Composable
private fun LoadingView() {
    Column {
        repeat(15){
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
    navController: NavController
) {
    PostList(
        posts = posts,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onPostClick = { post ->
            val json = Uri.encode(Gson().toJson(post))
            navController.navigate(Channel.PostView.name + "?post_json=$json")
        },
        isRefreshing = false
    )
}

@Preview
@Composable
private fun Preview(){
    Theme {
        val araMyPostViewModel = object : AraMyPostViewModelProtocol {
            override val posts = MutableStateFlow<List<AraPost>>(emptyList())
            override val state = MutableStateFlow<AraMyPostViewModel.ViewState>(AraMyPostViewModel.ViewState.Loaded(AraPost.mockList()))
            override var type = AraMyPostViewModel.PostType.ALL
            override var user: AraUser? = null
            override var searchKeyword: String = ""
            override fun bind() {}
            override suspend fun fetchInitialPosts() {}
            override suspend fun loadNextPage() {}
            override fun refreshItem(postID: Int) {}
        }

        AraMyPostView(
            viewModel = araMyPostViewModel,
            navController = rememberNavController()
        )
    }
}