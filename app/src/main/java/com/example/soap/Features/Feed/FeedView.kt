package com.example.soap.Features.Feed

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Repositories.Feed.FeedPostRepositoryProtocol
import com.example.soap.Features.Feed.Components.FeedPostRow
import com.example.soap.Features.Feed.Components.FeedPostRowSkeleton
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.ui.theme.Theme
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val repo: FeedPostRepositoryProtocol = hiltViewModel<FeedViewModel>().feedPostRepository
    val state by viewModel.state.collectAsState()
    val posts by viewModel.posts.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showCompose by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
    }

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Start,
                scrollOffset = scrollState.value
            )
        },

        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Start
            )
        }
    ) { padding ->
        when (state) {
            is FeedViewModel.ViewState.Loading -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    repeat(3){
                       item {
                           FeedPostRowSkeleton()
                           HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                       }
                    }
                }
            }
            is FeedViewModel.ViewState.Loaded -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(posts) { post ->
                        FeedPostRow(
                            post = post,
                            onPostDeleted = { postId ->
                                coroutineScope.launch { viewModel.deletePost(postId) }
                            },
                            onComment = {
                                val json = Uri.encode(Gson().toJson(post))
                                navController.navigate(Channel.FeedPost.name + "?feed_json=$json")
                            },
                            feedPostRepository = repo
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
            is FeedViewModel.ViewState.Error -> {
                val message = (state as FeedViewModel.ViewState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $message")
                }
            }
        }
    }

    if (showSettings) {
//        SettingsView(onDismiss = { showSettings = false })
    }

    if (showCompose) {
//        FeedPostComposeView(onDismiss = {
//            showCompose = false
//            viewModel.fetchInitialData()
//        })
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        FeedView(viewModel(), rememberNavController())
    }
}
