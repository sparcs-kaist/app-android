@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.soap.Features.PostList

import android.net.Uri
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Ara.AraBoard
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostList.PostList
import com.example.soap.Features.PostList.Components.PostListRow.BoardNavigationBar
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Views.ErrorView.ErrorView
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PostListView(
    board: AraBoard, //게시판 성격 (공지, 자게, 등등)
    postListViewModel: PostListViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    var loadedInitialPost by remember { mutableStateOf(false) }
    val searchText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(searchText) {
        postListViewModel.searchKeyword = searchText
    }

    LaunchedEffect(Unit) {
        if (!loadedInitialPost) {
            loadedInitialPost = true
            postListViewModel.board = board
            postListViewModel.fetchInitialPosts()
            postListViewModel.bind()
            loadedInitialPost = false
        }
    }

    Scaffold(
        topBar = { BoardNavigationBar(navController = navController) },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (!board.isReadOnly && board.userWritable == true) {
                ComposeButton(onClick = { navController.navigate(Channel.PostCompose.name) })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (val state = postListViewModel.state.collectAsState().value) {
                is PostListViewModel.ViewState.Loading -> {
                    Log.d("PostListView", "로딩중")
                    LoadingView()
                }
                is PostListViewModel.ViewState.Loaded -> {
                    Log.d("PostListView", state.posts.toString())
                    PostList(
                        posts = state.posts,
                        onLoadMore = {
                            coroutineScope.launch{
                            postListViewModel.loadNextPage()
                        } },
                        onRefresh = {
                            isRefreshing = true
                            coroutineScope.launch{
                                postListViewModel.fetchInitialPosts()
                                delay(500)
                                isRefreshing = false
                            }
                                    },
                        onPostClick = { post ->
                            val json = Uri.encode(Gson().toJson(post))
                            navController.navigate("postView?post_json=$json")
                        },
                        isRefreshing = isRefreshing
                    )
                }
                is PostListViewModel.ViewState.Error -> {
                    val error = (state).message
                    Log.d("PostListView", error)
                    ErrorView(
                        icon = Icons.Default.Warning,
                        errorMessage = error,
                        onRetry = {
                            coroutineScope.launch{
                                if (!loadedInitialPost) {
                                    postListViewModel.fetchInitialPosts()
                                    postListViewModel.bind()
                                    loadedInitialPost = true
                                }
                            }
                        }
                    )
                }
            }

            if (searchText.isNotEmpty() && postListViewModel.posts.isEmpty()) {
                EmptyView(
                    searchText = searchText,
                    onClear = {
                        CoroutineScope(Dispatchers.IO).launch {
                            postListViewModel.searchKeyword = ""
                            postListViewModel.fetchInitialPosts()
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
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
fun selectedColor(
    isSelected : Boolean
): Pair<Color, Color> {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainer
    val textColor =  if (isSelected) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.onSurface

    return Pair(backgroundColor, textColor)
}

@Composable
@Preview
private fun Preview(){
    Theme { PostListView(board = AraBoard.mock(), viewModel(), rememberNavController()) }

}