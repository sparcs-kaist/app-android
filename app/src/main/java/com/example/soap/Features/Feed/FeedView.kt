package com.example.soap.Features.Feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Feed.FeedPost
import com.example.soap.Features.Feed.Components.FeedPostRow
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModelProtocol = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val posts by viewModel.posts.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showCompose by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.fetchInitialData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                actions = {
                    IconButton(onClick = { showCompose = true }) {
                        Icon(Icons.Default.Create, contentDescription = "Write")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is FeedViewModel.ViewState.Loading -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(FeedPost.mockList()) { post ->
                        FeedPostRow(post = post, onPostDeleted = null, onComment = null)
                        HorizontalDivider()
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
                            onComment = { /* navigate to comments */ },
                            feedPostRepository = {}
                        )
                        HorizontalDivider()
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
        SettingsView(onDismiss = { showSettings = false })
    }

    if (showCompose) {
        FeedPostComposeView(onDismiss = {
            showCompose = false
            viewModel.fetchInitialData()
        })
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        FeedView(viewModel(), rememberNavController())
    }
}
