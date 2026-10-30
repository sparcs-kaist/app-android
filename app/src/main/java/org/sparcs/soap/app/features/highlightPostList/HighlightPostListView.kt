package org.sparcs.soap.app.features.highlightPostList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.components.sectionTitle
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.navigationBar.components.BackButton
import org.sparcs.soap.app.features.postList.components.postList.PostList
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewHighlightPostListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightPostListView(
    navController: NavController,
    viewModel: HighlightPostListViewModelProtocol = hiltViewModel<HighlightPostListViewModel>(),
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val topBarScrollBehavior = topAppBarScrollBehavior()

    LaunchedEffect(viewModel) {
        if (viewModel.state.value !is HighlightPostListViewModel.ViewState.Loaded) viewModel.fetchInitialPosts()
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.lastClickedPostId?.let { id ->
            viewModel.refreshItem(id)
            viewModel.lastClickedPostId = null
        }
        onPauseOrDispose {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton { navController.popBackStack() } },
                title = {
                    Text(
                        text = sectionTitle(viewModel.section, null),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                scrollBehavior = topBarScrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(name = "Highlight Post List")
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                when (val current = state) {
                    is HighlightPostListViewModel.ViewState.Error -> ErrorView(
                        error = current.error,
                        onRetry = viewModel::fetchInitialPosts
                    )

                    else -> PostList(
                        posts = (current as? HighlightPostListViewModel.ViewState.Loaded)?.posts,
                        onRefresh = viewModel::refresh,
                        onLoadMore = viewModel::loadNextPage,
                        onPostClick = { post: AraPost ->
                            viewModel.markRead(post.id)
                            viewModel.lastClickedPostId = post.id
                            navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                        },
                        isRefreshing = isRefreshing,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        rowPadding = PaddingValues(horizontal = 16.dp),
                        isLoadingMore = isLoadingMore
                    )
                }
            }
        }
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun LoadingPreview() {
    Theme {
        HighlightPostListView(
            navController = rememberNavController(),
            viewModel = PreviewHighlightPostListViewModel(HighlightPostListViewModel.ViewState.Loading)
        )
    }
}

@Preview(name = "Loaded", showBackground = true)
@Composable
private fun LoadedPreview() {
    Theme {
        HighlightPostListView(
            navController = rememberNavController(),
            viewModel = PreviewHighlightPostListViewModel(
                HighlightPostListViewModel.ViewState.Loaded(AraPost.mockList()),
                section = BoardListSection.Latest
            )
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun ErrorPreview() {
    Theme {
        HighlightPostListView(
            navController = rememberNavController(),
            viewModel = PreviewHighlightPostListViewModel(HighlightPostListViewModel.ViewState.Error(Exception()))
        )
    }
}
