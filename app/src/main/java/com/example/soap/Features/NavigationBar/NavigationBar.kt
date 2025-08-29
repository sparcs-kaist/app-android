package com.example.soap.Features.NavigationBar

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiLocation
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.BoardList.BoardListView
import com.example.soap.Features.Home.HomeView
import com.example.soap.Features.LectureDetail.LectureDetailView
import com.example.soap.Features.NavigationBar.Animation.trendingEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingExitTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopExitTransition
import com.example.soap.Features.NavigationBar.Components.AddButton
import com.example.soap.Features.NavigationBar.Components.ChatButton
import com.example.soap.Features.NavigationBar.Components.NotificationButton
import com.example.soap.Features.NavigationBar.Components.SettingButton
import com.example.soap.Features.Post.PostView
import com.example.soap.Features.PostCompose.PostComposeView
import com.example.soap.Features.PostList.PostListView
import com.example.soap.Features.TaxiChat.TaxiChatView
import com.example.soap.Features.TaxiChat.TaxiChatViewModel
import com.example.soap.Features.TaxiList.TaxiListView
import com.example.soap.Features.TaxiList.TaxiListViewModel
import com.example.soap.Features.TaxiRoomCreation.TaxiRoomCreationView
import com.example.soap.Features.Timetable.TimetableView
import com.example.soap.R
import com.example.soap.Shared.Mocks.mock
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.ViewModel.MockTaxiListViewModel
import com.example.soap.Shared.ViewModelMocks.MockTaxiChatViewModel
import com.example.soap.ui.theme.Theme

enum class Channel(@StringRes val title: Int) {
    Appname(title = R.string.app_name),
    Start(title = R.string.start),
    TimeTable(title = R.string.timetable),
    Taxi(title = R.string.taxi),
    TrendingBoard(title = R.string.general_board),
    Boards(title = R.string.boards),
    PostView(title = R.string.postview), //임시
    PostCompose(title = R.string.postcompose),
    LectureDetail(title= R.string.lecturedetail),//임시
    TaxiRoomCreation(title = R.string.taxi_room_creation),
    TaxiChatView(title = R.string.taxichatview)
}

@Composable
fun MainTabBar(navController: NavHostController = rememberNavController()) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Channel.entries.find { screen ->
        currentRoute?.startsWith(screen.name) == true
    } ?: Channel.Start

    val mockViewModel = MockTaxiListViewModel(
        initialState = TaxiListViewModel.ViewState.Loaded(
            rooms = TaxiRoom.mockList(),
            locations = TaxiLocation.mockList()
        )
    )

    val taxiChatMockViewModel = MockTaxiChatViewModel(
        initialState = TaxiChatViewModel.ViewState.Loaded(
            groupedChats = TaxiChatGroup.mockList(),
        ),
        initialGroupedChats = TaxiChatGroup.mockList(),
        initialTaxiUser = TaxiUser.mock(),
        initialUploading = false
    )

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        NavHost(
            navController = navController,
            startDestination = Channel.Start.name,
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            composable(
                route = Channel.Start.name,
            ) { HomeView(navController) }

            composable(
                route = Channel.Boards.name
            ) { BoardListView(navController) }

            composable(
                route = Channel.TimeTable.name
            ) { TimetableView(navController) }

            composable(
                route = Channel.Taxi.name
            ) {
                TaxiListView(mockViewModel, navController = navController)
            }

            composable(
                route = Channel.TaxiRoomCreation.name,
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) {
                TaxiRoomCreationView(navController, mockViewModel)
            }

            composable(
                route = Channel.TaxiChatView.name
            ) {
                TaxiChatView(taxiChatMockViewModel, navController)
            }

            composable(
                route = Channel.TrendingBoard.name
            ) { PostListView(navController = navController) }

            composable(
                route = Channel.PostView.name
            ) { PostView(navController = navController) }

            composable(
                route = Channel.PostCompose.name,
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { PostComposeView(postListViewModel = viewModel(), navController = navController) }

            composable(
                route = "${Channel.LectureDetail.name}/{lectureId}",
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { backStackEntry ->
                val lectureId = backStackEntry.arguments?.getString("lectureId")?.toIntOrNull()
                lectureId?.let {
                    LectureDetailView(lectureId = it, navController = navController)
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Channel,
    scrollOffset: Int = 0,
    navController: NavController = rememberNavController(),
    isButtonEnabled: Boolean = true
) {
    val elevationDp by animateDpAsState(
        if (scrollOffset > 0) 4.dp else 0.dp,
        label = "ElevationAnimation"
    )

    TopAppBar(
        title = {
            Row{
                Text(
                    text = stringResource(currentScreen.title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            when (currentScreen) {
                Channel.Start -> {
                    NotificationButton()
                    SettingButton()
                }
                Channel.TimeTable -> {
                    AddButton(
                        contentDescription = "Add Timetable",
                        onClick = {}
                    )
                }
                Channel.Taxi -> {
                    if(isButtonEnabled) {
                        AddButton(
                            contentDescription = "Create Taxi Room",
                            onClick = { navController.navigate(Channel.TaxiRoomCreation.name) }
                        )
                    }
                    ChatButton(onClick = { navController.navigate(Channel.TaxiChatView.name) } )
                }//Todo-TaxiChat -> TaxiChat List
                else -> {}
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.shadow(elevationDp)
    )
}



@Composable
fun AppDownBar(
    navController: NavController,
    currentScreen: Channel
) {
    val items = listOf(
        Triple(Channel.Start, stringResource(Channel.Start.title), R.drawable.round_home),
        Triple(Channel.Boards, stringResource(Channel.Boards.title), R.drawable.round_format_list_bulleted),
        Triple(Channel.TimeTable, stringResource(Channel.TimeTable.title), R.drawable.timetable),
        Triple(Channel.Taxi, stringResource(Channel.Taxi.title), R.drawable.taxi),
        Triple(null, stringResource(R.string.search), R.drawable.search)
    )
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .shadow(
                elevation = 8.dp
            )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            items.forEach { (channel, label, iconRes) ->
                if (channel != null) {
                    NavigationBarItem(
                        selected = currentScreen == channel,
                        onClick = { navController.navigate(channel.name) },
                        icon = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(MaterialTheme.colorScheme.primary)
                    )
                }else{
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label
                            )
                        },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(MaterialTheme.colorScheme.primary)
                    )
                }
            }

        }
    }
}



@Preview
@Composable
private fun Preview(){
    Theme {
        MainTabBar()
    }
}


@Preview
@Composable
private fun AppBarPreview(){
   Theme { AppBar(
       currentScreen = Channel.Start
   ) }
}

@Preview
@Composable
private fun AppDownBarPreview(){
    Theme {
        AppDownBar(
            navController = rememberNavController(),
            currentScreen = Channel.Start
        )
    }
}
