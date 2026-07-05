package org.sparcs.soap.app.features.boardList

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Drafts
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.NotificationsActive
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
import org.sparcs.soap.app.domain.models.ara.AraBoardGroup
import org.sparcs.soap.app.features.boardList.components.BoardList
import org.sparcs.soap.app.features.boardList.components.BoardListNavigationBar
import org.sparcs.soap.app.features.boardList.components.BoardListSectionItem
import org.sparcs.soap.app.features.boardList.components.BoardListSkeleton
import org.sparcs.soap.app.features.navigationBar.AppDownBar
import org.sparcs.soap.app.features.navigationBar.Channel
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.views.contentViews.ErrorView
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.buddyPreviewSupport.post.PreviewBoardListViewModel

@Composable
fun BoardListView(
    viewModel: BoardListViewModelProtocol = hiltViewModel<BoardListViewModel>(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    val backStackEvent = {
        navController.navigate(Channel.Start.name) {
            popUpTo(0) { inclusive = true }
        }
    }

    BackHandler {
        backStackEvent()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBoards()
    }

    Scaffold(
        topBar = {
            BoardListNavigationBar(
                scrollState = scrollState
            )
        },
        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Boards
            )
        },
        modifier = Modifier.analyticsScreen(name = "Board List")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val onBoardClick: (AraBoard) -> Unit = { board ->
                        val json = Uri.encode(Gson().toJson(board))
                        navController.navigate(Channel.BoardList.name + "?board_json=$json")
                    }

                    if (state is BoardListViewModel.ViewState.Loading) {
                        BoardListSkeleton(4)
                        BoardListSkeleton(1)
                        BoardListSkeleton(2)
                        BoardListSkeleton(3)
                    } else if (state is BoardListViewModel.ViewState.Loaded) {
                        val loaded = state as BoardListViewModel.ViewState.Loaded
                        loaded.groups.forEach { group ->
                            BoardGroupItem(group, loaded.boards, onBoardClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardGroupItem(
    group: AraBoardGroup,
    boards: List<AraBoard>,
    onBoardClick: (AraBoard) -> Unit
) {
    val boardsInGroup = boards.filter { it.group.id == group.id }
    BoardList(
        title = group.name.localized(),
        icon = systemImage(group.slug),
        sections = listOf({
            boardsInGroup.forEach { board ->
                BoardListSectionItem(
                    text = board.name.localized(),
                    onClick = { onBoardClick(board) }
                )
            }
        })
    )
}

@Composable
fun systemImage(slug: String): ImageVector {
    return when (slug) {
        "notice" -> Icons.Rounded.NotificationsActive
        "talk" -> Icons.AutoMirrored.Rounded.Chat
        "club" -> Icons.Rounded.Group
        "trade" -> Icons.Rounded.LocalOffer
        "communication" -> Icons.Rounded.Drafts
        else -> Icons.AutoMirrored.Rounded.FormatListBulleted
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
