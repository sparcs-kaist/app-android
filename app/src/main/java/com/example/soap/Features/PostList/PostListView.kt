@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.soap.Features.PostList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.soap.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun PostListView(
    board: AraBoard,
    postListViewModel: PostListViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    var loadedInitialPost by remember { mutableStateOf(false) }
    val searchText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(searchText) {
        postListViewModel.searchKeyword = searchText
    }

    LaunchedEffect(Unit) {
        if (!loadedInitialPost) {
            postListViewModel.bind()
            postListViewModel.fetchInitialPosts()
            loadedInitialPost = true
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
                    LoadingView()
                }
                is PostListViewModel.ViewState.Loaded -> {
                    PostList(
                        posts = state.posts,
                        onLoadMore = { coroutineScope.launch{ postListViewModel.loadNextPage() } },
                        onRefresh = { coroutineScope.launch{ postListViewModel.fetchInitialPosts() } },
                        navController = navController
                    )
                }
                is PostListViewModel.ViewState.Error -> {
                    // TODO - ErrorView
                }
            }

            if (searchText.isNotEmpty() && postListViewModel.posts.isEmpty()) {
               // TODO - ErrorView
            }
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