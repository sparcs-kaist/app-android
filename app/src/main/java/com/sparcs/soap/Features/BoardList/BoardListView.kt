package com.sparcs.soap.Features.BoardList

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.Ara.AraBoard
import com.sparcs.soap.Domain.Models.Ara.AraBoardGroup
import com.sparcs.soap.Features.BoardList.Components.BoardList
import com.sparcs.soap.Features.BoardList.Components.BoardListNavigationBar
import com.sparcs.soap.Features.BoardList.Components.BoardListSectionItem
import com.sparcs.soap.Features.BoardList.Components.BoardListSkeleton
import com.sparcs.soap.Features.NavigationBar.AppDownBar
import com.sparcs.soap.Features.NavigationBar.Channel
import com.sparcs.soap.R
import com.sparcs.soap.Shared.Mocks.mockList
import com.sparcs.soap.Shared.Views.ContentViews.ErrorView
import com.sparcs.soap.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun BoardListView(
    viewModel: BoardListViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { viewModel.fetchBoards() }
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
        }
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
                    val message = (state as BoardListViewModel.ViewState.Error).message

                        ErrorView(
                            icon = Icons.Default.Warning,
                            errorMessage = message,
                            onRetry = { scope.launch { viewModel.fetchBoards() } }
                        )
                    }
                }
            }
    }
}

@Composable
private fun LoadingView(){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
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
    onBoardClick: (AraBoard) -> Unit
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
fun systemImage(slug: String): Painter {
    return when (slug) {
        "notice" -> painterResource(R.drawable.round_notifications_active)
        "talk" -> painterResource(R.drawable.round_chat)
        "club" -> painterResource(R.drawable.group)
        "trade" -> painterResource(R.drawable.baseline_local_offer)
        "communication" -> painterResource(R.drawable.baseline_drafts)
        else -> painterResource(R.drawable.round_format_list_bulleted)
    }
}


@Composable
@Preview
private fun Preview() {
    Theme{
        LoadedView(
            boards = AraBoard.mockList(),
            groups = listOf(
                AraBoardGroup(id = 123, slug = "slug", name = LocalizedString(mapOf("en" to "Group Name", "ko" to "그룹"))),
                ),
            onBoardClick = {}
        )
    }
}