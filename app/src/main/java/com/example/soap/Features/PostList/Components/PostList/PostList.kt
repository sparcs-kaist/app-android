package com.example.soap.Features.PostList.Components.PostList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Ara.AraPost
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Features.PostList.Components.PostListRow.PostListRow
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.lightGray0


@Composable
fun PostList(
    posts: List<AraPost>?,
    onRefresh: (suspend () -> Unit),
    onLoadMore: (suspend () -> Unit),
    onPostClick: (AraPost) -> Unit,
    navController: NavController
) {
    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    if (posts != null && posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nothing Here Yet\nIt looks like there are no posts on this page right now."
            )//TODO - ErrorView
        }
    } else {
        val shouldLoadMore by remember {
            derivedStateOf {
                val visibleItems = listState.layoutInfo.visibleItemsInfo
                if (posts == null || visibleItems.isEmpty()) return@derivedStateOf false
                val lastVisible = visibleItems.last().index
                lastVisible >= (posts.size * 0.6).toInt()
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore && !isLoadingMore) {
                isLoadingMore = true
                onLoadMore.invoke()
                isLoadingMore = false
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            when {
                posts == null -> {
                    items(AraPost.mockList()) { post ->
                        PostListRow(post = post)
                    }
                }
                else -> {
                    items(posts) { post ->
                        PostListRow(
                            post = post,
                            modifier = Modifier
                                .clickable(enabled = !post.isHidden) {onPostClick(post)}
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.lightGray0)
                    }

                    if (isLoadingMore) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadedView(
    posts: List<AraPost>,
    isLoadingMore: Boolean,
    onLoadMore: (suspend () -> Unit),
    navController: NavController
) {
    LazyColumn {
        itemsIndexed(posts) { index, post ->
            PostListRow(post = post)

            if (!post.isHidden) {
                Box(
                    modifier = Modifier
                        .clickable {
                            navController.navigate(Channel.PostView.name)
                        }
                )
            }

            val thresholdIndex = (posts.size * 0.6).toInt()
            if (index >= thresholdIndex) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        if (isLoadingMore) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    LazyColumn {
        items(AraPost.mockList()) { post ->
            PostListRow(post = post)
        }
    }
}


@Composable
@Preview
private fun LoadingPreview(){
    Theme {
        PostList(posts = null, onRefresh = {}, onLoadMore = {}, onPostClick = {},rememberNavController())
    }
}

@Composable
@Preview
private fun EmptyPreview(){
    Theme {
        PostList(posts = emptyList(), onRefresh = {}, onLoadMore = {},onPostClick = {}, rememberNavController())
    }
}

@Composable
@Preview
private fun LoadedPreview(){
    Theme {
        PostList(posts = AraPost.mockList(), onRefresh = {}, onLoadMore = {}, onPostClick = {}, rememberNavController())
    }
}
