package org.sparcs.soap.App.Features.BoardList

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Ara.AraBoard
import org.sparcs.soap.App.Domain.Models.Ara.AraBoardGroup
import org.sparcs.soap.App.Features.BoardList.Components.BoardList
import org.sparcs.soap.App.Features.BoardList.Components.BoardListNavigationBar
import org.sparcs.soap.App.Features.BoardList.Components.BoardListSectionItem
import org.sparcs.soap.App.Features.BoardList.Components.BoardListSkeleton
import org.sparcs.soap.App.Features.NavigationBar.AppDownBar
import org.sparcs.soap.App.Features.NavigationBar.Channel
import org.sparcs.soap.App.Shared.Extensions.analyticsScreen
import org.sparcs.soap.App.Shared.Views.ContentViews.ErrorView
import org.sparcs.soap.BuddyPreviewSupport.Post.PreviewBoardListViewModel
import org.sparcs.soap.R

@Composable
fun BoardListView(
    viewModel: BoardListViewModelProtocol = hiltViewModel(),
    navController: NavController,
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            when (state) {
                is BoardListViewModel.ViewState.Loading -> {
                    LoadingView()
                }

                is BoardListViewModel.ViewState.Loaded -> {
                    val loadedState = state as BoardListViewModel.ViewState.Loaded
                    LoadedView(
                        boards = loadedState.boards,
                        groups = loadedState.groups,
                        onBoardClick = { board ->
                            val json = Uri.encode(Gson().toJson(board))
                            navController.navigate(Channel.BoardList.name + "?board_json=$json")
                        }
                    )
                }

                is BoardListViewModel.ViewState.Error -> {
                    val error = (state as BoardListViewModel.ViewState.Error).error

                    ErrorView(
                        icon = Icons.Default.Warning,
                        error = error,
                        defaultMessageResId = R.string.error_fetch_boards,
                        onRetry = { scope.launch { viewModel.fetchBoards() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BoardListSkeleton(4)
        BoardListSkeleton(1)
        BoardListSkeleton(2)
        BoardListSkeleton(3)
    }
}

@Composable
private fun LoadedView(
    boards: List<AraBoard>,
    groups: List<AraBoardGroup>,
    onBoardClick: (AraBoard) -> Unit,
) {
    groups.forEach { group ->
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
    BoardListView(viewModel, rememberNavController())
}

@Preview(name = "Loaded State", showBackground = true)
@Composable
fun PreviewBoardListLoaded() {
    val viewModel = PreviewBoardListViewModel(
        initialState = PreviewBoardListViewModel.loadedState()
    )
    BoardListView(viewModel, rememberNavController())
}

@Preview(name = "Error State", showBackground = true)
@Composable
fun PreviewBoardListError() {
    val viewModel = PreviewBoardListViewModel(
        initialState = BoardListViewModel.ViewState.Error(Exception())
    )
    BoardListView(viewModel, rememberNavController())
}