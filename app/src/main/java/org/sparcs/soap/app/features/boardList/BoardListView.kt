package org.sparcs.soap.app.features.boardList

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.domain.models.ara.AraPost
import org.sparcs.soap.app.domain.models.ara.BoardListSection
import org.sparcs.soap.app.features.boardList.components.BoardHighlightSection
import org.sparcs.soap.app.features.boardList.components.BoardListNavigationBar
import org.sparcs.soap.app.features.boardList.components.NewPostsDot
import org.sparcs.soap.app.features.boardList.components.boardListSkeleton
import org.sparcs.soap.app.features.boardList.components.icon
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
import org.sparcs.soap.app.shared.mocks.ara.mockList
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewBoardListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BoardListView(
    viewModel: BoardListViewModelProtocol = hiltViewModel<BoardListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchBoards()
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.reloadSections()
        onPauseOrDispose {}
    }

    val topBarScrollBehavior = topAppBarScrollBehavior()

    Scaffold(
        topBar = {
            BoardListNavigationBar(
                scrollBehavior = topBarScrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .hideTopBarOnScroll(topBarScrollBehavior)
            .analyticsScreen(name = "Board List")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (state is BoardListViewModel.ViewState.Error) {
                val error = (state as BoardListViewModel.ViewState.Error).error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorView(
                        error = error,
                        defaultMessageResId = R.string.error_fetch_boards,
                        onRetry = { viewModel.fetchBoards() }
                    )
                }
            } else {
                val onBoardClick: (AraBoard) -> Unit = { board ->
                    val json = Uri.encode(Gson().toJson(board))
                    navController.navigate(Channel.BoardList.name + "?board_json=$json")
                }
                val onPostClick: (AraPost) -> Unit = { post ->
                    navController.navigate(Channel.PostView.name + "?postId=${post.id}")
                }
                val onSeeAllHighlight: (BoardListSection) -> Unit = { section ->
                    navController.navigate(Channel.HighlightPostList.name + "?section=${section.id}")
                }
                val loadedBoards = (state as? BoardListViewModel.ViewState.Loaded)?.boards

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    viewModel.sections.forEach { section ->
                        val board = (section as? BoardListSection.Board)?.let { s ->
                            loadedBoards?.firstOrNull { it.slug == s.slug && it.userReadable != false }
                        }
                        val isMissingBoard = section is BoardListSection.Board && loadedBoards != null && board == null
                        if (!isMissingBoard) item(key = "section-${section.id}") {
                            BoardHighlightSection(
                                section = section,
                                board = board,
                                posts = viewModel.posts(section),
                                onPostClick = onPostClick,
                                onSeeAll = if (board != null) {
                                    { onBoardClick(board) }
                                } else {
                                    { onSeeAllHighlight(section) }
                                }
                            )
                        }
                    }
                    when (val current = state) {
                        is BoardListViewModel.ViewState.Loading -> boardListSkeleton(listOf(4, 1, 2, 3))
                        is BoardListViewModel.ViewState.Loaded -> current.groups.forEach { group ->
                            stickyHeader(key = "group-${group.id}") {
                                SettingsSectionHeader(
                                    text = group.name.localized(),
                                    icon = group.icon,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                )
                            }
                            items(
                                current.boards.filter { it.group.id == group.id },
                                key = { "board-${it.id}" }
                            ) { board ->
                                val newPostsLabel = stringResource(R.string.new_posts)
                                SettingsNavigationItem(
                                    text = board.name.localized(),
                                    onClick = { onBoardClick(board) },
                                    trailingContent = if (board.id in viewModel.boardsWithNewPosts) {
                                        { NewPostsDot(Modifier.semantics { contentDescription = newPostsLabel }) }
                                    } else null
                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
fun PreviewBoardListLoading() {
    val viewModel = PreviewBoardListViewModel(
        initialState = BoardListViewModel.ViewState.Loading,
        sections = BoardListSection.defaults
    )
    Theme { BoardListView(viewModel, rememberNavController()) }
}

@Preview(name = "Loaded State", showBackground = true)
@Composable
fun PreviewBoardListLoaded() {
    val viewModel = PreviewBoardListViewModel(
        initialState = PreviewBoardListViewModel.loadedState(),
        sections = BoardListSection.defaults,
        sectionPosts = previewSectionPosts()
    )
    Theme { BoardListView(viewModel, rememberNavController()) }
}

private fun previewSectionPosts(): Map<BoardListSection, List<AraPost>> {
    val posts = AraPost.mockList()
    return mapOf(
        BoardListSection.Trending to posts.take(3),
        BoardListSection.Latest to posts.takeLast(3),
        BoardListSection.Board(AraBoard.MARKET_SLUG) to posts
    )
}

@Preview(name = "Loaded State Landscape", showBackground = true, widthDp = 840, heightDp = 480)
@Composable
fun PreviewBoardListLoadedLandscape() {
    val viewModel = PreviewBoardListViewModel(initialState = PreviewBoardListViewModel.loadedState())
    Theme { BoardListView(viewModel, rememberNavController()) }
}

@Preview(name = "Error State", showBackground = true)
@Composable
fun PreviewBoardListError() {
    val viewModel = PreviewBoardListViewModel(initialState = BoardListViewModel.ViewState.Error(Exception()))
    Theme { BoardListView(viewModel, rememberNavController()) }
}
