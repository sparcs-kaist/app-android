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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.ara.AraBoard
import org.sparcs.soap.app.features.boardList.components.BoardListNavigationBar
import org.sparcs.soap.app.features.boardList.components.boardListSkeleton
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.hideTopBarOnScroll
import org.sparcs.soap.app.shared.extensions.topAppBarScrollBehavior
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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    when (val current = state) {
                        is BoardListViewModel.ViewState.Loading -> boardListSkeleton(listOf(4, 1, 2, 3))
                        is BoardListViewModel.ViewState.Loaded -> current.groups.forEach { group ->
                            // Each group's header stays pinned while its boards scroll beneath it.
                            stickyHeader(key = "group-${group.id}") {
                                SettingsSectionHeader(
                                    text = group.name.localized(),
                                    icon = systemImage(group.slug),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                )
                            }
                            items(
                                current.boards.filter { it.group.id == group.id },
                                key = { "board-${it.id}" }
                            ) { board ->
                                SettingsNavigationItem(
                                    text = board.name.localized(),
                                    onClick = { onBoardClick(board) }
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

fun systemImage(slug: String): ImageVector {
    return when (slug) {
        "notice" -> Icons.Outlined.NotificationsActive
        "talk" -> Icons.AutoMirrored.Outlined.Chat
        "club" -> Icons.Outlined.Group
        "trade" -> Icons.Outlined.LocalOffer
        "communication" -> Icons.Outlined.Drafts
        else -> Icons.AutoMirrored.Outlined.FormatListBulleted
    }
}

// MARK: - Previews
@Preview(name = "Loading State", showBackground = true)
@Composable
fun PreviewBoardListLoading() {
    val viewModel = PreviewBoardListViewModel(initialState = BoardListViewModel.ViewState.Loading)
    Theme { BoardListView(viewModel, rememberNavController()) }
}

@Preview(name = "Loaded State", showBackground = true)
@Composable
fun PreviewBoardListLoaded() {
    val viewModel = PreviewBoardListViewModel(initialState = PreviewBoardListViewModel.loadedState())
    Theme { BoardListView(viewModel, rememberNavController()) }
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
