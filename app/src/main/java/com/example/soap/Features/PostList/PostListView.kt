@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.soap.Features.PostList

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostList.PostList
import com.example.soap.Features.PostList.Components.PostListRow.BoardNavigationBar
import com.example.soap.Features.PostList.Components.PostListRow.PostListSkeletonRow
import com.example.soap.R
import com.example.soap.Shared.Views.ContentViews.ErrorView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.lightGray0
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PostListView(
    viewModel: PostListViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    var loadedInitialPost by remember { mutableStateOf(false) }
    val searchText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val board = viewModel.board

    val backStackEntry = navController.currentBackStackEntry!!
    LaunchedEffect(searchText) {
        viewModel.searchKeyword = searchText
    }

    LaunchedEffect(Unit) {
        val json = Gson().toJson(board)
        backStackEntry.savedStateHandle["board_json"] = json
        if (!loadedInitialPost) {
            loadedInitialPost = true
            viewModel.board = board
            viewModel.bind()
            loadedInitialPost = false
        }
    }

    Scaffold(
        topBar = {
            BoardNavigationBar(
                title = board.name.localized(),
            subTitle = board.group.name.localized(),
            navController = navController
        )
                 },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (!board.isReadOnly && board.userWritable == true) {
                ComposeButton(
                    onClick = {
                        val json = Uri.encode(Gson().toJson(board))
                        navController.navigate(Channel.PostCompose.name + "?board_json=$json")
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (val state = viewModel.state.collectAsState().value) {
                is PostListViewModel.ViewState.Loading -> {
                    LoadingView()
                }
                is PostListViewModel.ViewState.Loaded -> {
                    PostList(
                        posts = state.posts,
                        onLoadMore = {
                            coroutineScope.launch {
                            viewModel.loadNextPage()
                        } },
                        onRefresh = {
                            isRefreshing = true
                            coroutineScope.launch {
                                viewModel.fetchInitialPosts()
                                delay(500)
                                isRefreshing = false
                            }
                                    },
                        onPostClick = { post ->
                            val json = Uri.encode(Gson().toJson(post))
                            navController.navigate(Channel.PostView.name + "?post_json=$json")
                        },
                        onPostDisappear = { postID -> viewModel.refreshItem(postID)},
                        isRefreshing = isRefreshing
                    )
                }
                is PostListViewModel.ViewState.Error -> {
                    val error = (state).message
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = error,
                        onRetry = {
                            coroutineScope.launch {
                                if (!loadedInitialPost) {
                                    viewModel.fetchInitialPosts()
                                    viewModel.bind()
                                    loadedInitialPost = true
                                }
                            }
                        }
                    )
                }
            }

            if (searchText.isNotEmpty() && viewModel.posts.isEmpty()) {
                EmptyView(
                    searchText = searchText,
                    onClear = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.searchKeyword = ""
                            viewModel.fetchInitialPosts()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyView(
    searchText: String,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.round_error_outline),
            contentDescription = "No result",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.grayBB
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No result",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No results found for \"$searchText\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onClear() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Search Text")
        }
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
private fun ComposeButton(onClick: () -> Unit){
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        contentPadding = PaddingValues(horizontal = 5.dp, vertical = 15.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.outline_edit),
            contentDescription = "Write Button",
            tint = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { PostListView(viewModel(), rememberNavController()) }

}