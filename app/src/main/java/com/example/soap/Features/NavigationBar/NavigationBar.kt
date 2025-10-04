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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.soap.Domain.Usecases.MockTimetableUseCase
import com.example.soap.Features.BoardList.BoardListView
import com.example.soap.Features.BoardList.BoardListViewModel
import com.example.soap.Features.Feed.FeedView
import com.example.soap.Features.Feed.FeedViewModel
import com.example.soap.Features.Feed.FeedViewModelProtocol
import com.example.soap.Features.FeedPost.FeedPostView
import com.example.soap.Features.FeedPost.FeedPostViewModel
import com.example.soap.Features.FeedPost.FeedPostViewModelProtocol
import com.example.soap.Features.FeedPostCompose.FeedPostComposeView
import com.example.soap.Features.FeedPostCompose.FeedPostComposeViewModel
import com.example.soap.Features.FeedPostCompose.FeedPostComposeViewModelProtocol
import com.example.soap.Features.Home.Components.HomeViewDropDownMenu
import com.example.soap.Features.LectureDetail.LectureDetailView
import com.example.soap.Features.NavigationBar.Animation.trendingEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingExitTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopExitTransition
import com.example.soap.Features.NavigationBar.Components.AddButton
import com.example.soap.Features.NavigationBar.Components.ChatButton
import com.example.soap.Features.Post.PostView
import com.example.soap.Features.Post.PostViewModel
import com.example.soap.Features.Post.PostViewModelProtocol
import com.example.soap.Features.PostCompose.PostComposeView
import com.example.soap.Features.PostCompose.PostComposeViewModel
import com.example.soap.Features.PostCompose.PostComposeViewModelProtocol
import com.example.soap.Features.PostList.PostListView
import com.example.soap.Features.PostList.PostListViewModel
import com.example.soap.Features.PostList.PostListViewModelProtocol
import com.example.soap.Features.Settings.Ara.AraMyPostView
import com.example.soap.Features.Settings.Ara.AraMyPostViewModel
import com.example.soap.Features.Settings.Ara.AraMyPostViewModelProtocol
import com.example.soap.Features.Settings.Ara.AraSettingsView
import com.example.soap.Features.Settings.Ara.AraSettingsViewModel
import com.example.soap.Features.Settings.Ara.AraSettingsViewModelProtocol
import com.example.soap.Features.Settings.SettingsView
import com.example.soap.Features.Settings.Taxi.TaxiReportListView
import com.example.soap.Features.Settings.Taxi.TaxiReportListViewModel
import com.example.soap.Features.Settings.Taxi.TaxiReportListViewModelProtocol
import com.example.soap.Features.Settings.Taxi.TaxiSettingsView
import com.example.soap.Features.Settings.Taxi.TaxiSettingsViewModel
import com.example.soap.Features.Settings.Taxi.TaxiSettingsViewModelProtocol
import com.example.soap.Features.SignIn.SignInView
import com.example.soap.Features.SignIn.SignInViewModel
import com.example.soap.Features.TaxiChat.TaxiChatView
import com.example.soap.Features.TaxiChat.TaxiChatViewModel
import com.example.soap.Features.TaxiChat.TaxiChatViewModelProtocol
import com.example.soap.Features.TaxiChatList.TaxiChatListView
import com.example.soap.Features.TaxiChatList.TaxiChatListViewModel
import com.example.soap.Features.TaxiChatList.TaxiChatListViewModelProtocol
import com.example.soap.Features.TaxiList.TaxiListView
import com.example.soap.Features.TaxiList.TaxiListViewModel
import com.example.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.example.soap.Features.TaxiReport.TaxiReportView
import com.example.soap.Features.TaxiReport.TaxiReportViewModel
import com.example.soap.Features.TaxiRoomCreation.TaxiRoomCreationView
import com.example.soap.Features.Timetable.TimetableView
import com.example.soap.Features.Timetable.TimetableViewModel
import com.example.soap.Features.UserPostList.UserPostListView
import com.example.soap.Features.UserPostList.UserPostListViewModel
import com.example.soap.Features.UserPostList.UserPostListViewModelProtocol
import com.example.soap.R
import com.example.soap.ui.theme.Theme

enum class Channel(@StringRes val title: Int) {
    Appname(title = R.string.app_name),
    Start(title = R.string.start),
    FeedPost(title = R.string.feed_post_view),
    FeedPostCompose(title = R.string.feed_post_compose_view),
    TimeTable(title = R.string.timetable),
    Taxi(title = R.string.taxi),
    BoardList(title = R.string.general_board),
    Boards(title = R.string.boards),
    PostView(title = R.string.postview),
    PostCompose(title = R.string.postcompose),
    LectureDetail(title = R.string.lecturedetail),
    TaxiRoomCreation(title = R.string.taxi_room_creation),
    TaxiChatView(title = R.string.taxichatview),
    TaxiChatListView(title = R.string.taxichatlistview),
    TaxiReportView(title = R.string.taxi_report_view),
    AraChatView(title = R.string.ara_chat_view), //임시
    UserPostListView(title = R.string.user_post_list_view),
    SearchView(title = R.string.search),
    SignOut(title = R.string.sign_out),
    Settings(title = R.string.settings),
    TaxiSettings(title = R.string.taxi_settings),
    TaxiReportSettings(title = R.string.taxi_report_settings),
    AraSettings(title = R.string.ara_settings),
    AraMyPostSettings(title = R.string.ara_my_post_settings)
}

@Composable
fun MainTabBar(navController: NavHostController = rememberNavController()) {

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Channel.entries.find { screen ->
        currentRoute?.startsWith(screen.name) == true
    } ?: Channel.Start

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
            ) { backStackEntry ->
                val viewModelImpl: FeedViewModel = hiltViewModel(backStackEntry)
                val viewModel: FeedViewModelProtocol = viewModelImpl
                FeedView(navController = navController, viewModel = viewModel)
            }

            composable(
                route = Channel.FeedPost.name + "?feed_json={feed_json}",
                arguments = listOf(
                    navArgument("feed_json") {
                        type = NavType.StringType
                        nullable = false
                    }
                ),
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { backStackEntry ->
                val viewModelImpl: FeedPostViewModel = hiltViewModel(backStackEntry)
                val viewModel: FeedPostViewModelProtocol = viewModelImpl
                FeedPostView(navController = navController, viewModel = viewModel)
            }

            composable(
                route = Channel.FeedPostCompose.name,
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { backStackEntry ->
                val viewModelImpl: FeedPostComposeViewModel = hiltViewModel(backStackEntry)
                val viewModel: FeedPostComposeViewModelProtocol = viewModelImpl

                FeedPostComposeView(navController = navController, viewModel = viewModel)
            }

//            }
            /*___________OTL___________*/
            val mockVm = TimetableViewModel(timetableUseCase = MockTimetableUseCase())
            composable(
                route = Channel.TimeTable.name
            ) { TimetableView(viewModel =  mockVm,navController = navController) }

            composable(
                route = Channel.LectureDetail.name + "?lecture_json={lecture_json}",   enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { backStackEntry ->
                val lectureId = backStackEntry.arguments?.getString("lectureId")?.toIntOrNull()

                LectureDetailView(onAdd = null, navController = navController)
            }

            /*___________Taxi___________*/
            navigation(
                startDestination = Channel.Taxi.name,
                route = "TaxiGraph"
            ) {
                composable(Channel.Taxi.name) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("TaxiGraph")
                    }
                    val viewModel: TaxiListViewModelProtocol =
                        hiltViewModel<TaxiListViewModel>(parentEntry)

                    TaxiListView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiRoomCreation.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = trendingPopEnterTransition(),
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("TaxiGraph")
                    }
                    val viewModel: TaxiListViewModelProtocol =
                        hiltViewModel<TaxiListViewModel>(parentEntry)

                    TaxiRoomCreationView(
                        navController = navController,
                        viewModel = viewModel
                    )
                }

                composable(
                    route = Channel.TaxiChatView.name + "?room_json={room_json}",
                    arguments = listOf(
                        navArgument("room_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val viewModelImpl: TaxiChatViewModel = hiltViewModel(backStackEntry)
                    val viewModel: TaxiChatViewModelProtocol = viewModelImpl
                    TaxiChatView(
                        viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiChatListView.name
                ) { backStackEntry ->
                    val viewModelImpl: TaxiChatListViewModel = hiltViewModel(backStackEntry)
                    val viewModel: TaxiChatListViewModelProtocol = viewModelImpl

                    TaxiChatListView(viewModel, navController)
                }

                composable(
                    route = Channel.TaxiReportView.name + "?room_json={room_json}",
                    arguments = listOf(
                        navArgument("room_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val viewModelImpl: TaxiReportViewModel = hiltViewModel(backStackEntry)
                    TaxiReportView(viewModelImpl, navController)
                }
            }

            /*___________Ara___________*/
            navigation(
                startDestination = Channel.Boards.name,
                route = "AraGraph"
            ) {
                composable(
                    route = Channel.Boards.name
                ) { backStackEntry ->
                    val viewModel: BoardListViewModel = hiltViewModel(backStackEntry)
                    BoardListView(viewModel = viewModel, navController = navController)
                }

                composable(
                    route = Channel.BoardList.name + "?board_json={board_json}",
                    arguments = listOf(
                        navArgument("board_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->

                    val viewModelImpl: PostListViewModel = hiltViewModel(backStackEntry)
                    val viewModel: PostListViewModelProtocol = viewModelImpl
                    PostListView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.PostView.name + "?post_json={post_json}",
                    arguments = listOf(
                        navArgument("post_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val viewModelImpl: PostViewModel = hiltViewModel(backStackEntry)
                    val viewModel: PostViewModelProtocol = viewModelImpl
                    PostView(viewModel = viewModel, navController = navController)
                }

                composable(
                    route = Channel.PostCompose.name + "?board_json={board_json}",
                    arguments = listOf(
                        navArgument("board_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = trendingPopEnterTransition(),
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModelImpl: PostComposeViewModel = hiltViewModel(backStackEntry)
                    val viewModel: PostComposeViewModelProtocol = viewModelImpl
                    PostComposeView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.UserPostListView.name + "?author_json={author_json}",
                    arguments = listOf(
                        navArgument("author_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = trendingPopEnterTransition(),
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModelImpl: UserPostListViewModel = hiltViewModel(backStackEntry)
                    val viewModel: UserPostListViewModelProtocol = viewModelImpl
                    UserPostListView(viewModel = viewModel, navController = navController)
                }
            }

            composable(
                route = Channel.SearchView.name,
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) {
                // SearchView()
            }

            composable(
                route = Channel.SignOut.name
            ) { backStackEntry ->
                val viewModelImpl: SignInViewModel = hiltViewModel(backStackEntry)
                LaunchedEffect(Unit) { viewModelImpl.authUseCase.signOut() }
                SignInView(viewModelImpl)
            }

            /*___________Settings___________*/
            navigation(
                startDestination = Channel.Settings.name,
                route = "SettingsGraph"
            ) {
                composable(
                    route = Channel.Settings.name
                ) { SettingsView(navController = navController) }

                composable(
                    route = Channel.AraSettings.name
                ) { backStackEntry ->
                    val viewModelImpl: AraSettingsViewModel = hiltViewModel(backStackEntry)
                    val viewModel: AraSettingsViewModelProtocol = viewModelImpl

                    AraSettingsView(viewModel = viewModel, navController = navController)
                }

                composable(
                    route = Channel.AraMyPostSettings.name + "?type_json={type_json}",
                    arguments = listOf(
                        navArgument("type_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val viewModelImpl: AraMyPostViewModel = hiltViewModel(backStackEntry)
                    val viewModel: AraMyPostViewModelProtocol = viewModelImpl

                    AraMyPostView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiSettings.name
                ) { backStackEntry ->
                    val viewModelImpl: TaxiSettingsViewModel = hiltViewModel(backStackEntry)
                    val viewModel: TaxiSettingsViewModelProtocol = viewModelImpl

                    TaxiSettingsView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiReportSettings.name
                ) { backStackEntry ->
                    val viewModelImpl: TaxiReportListViewModel = hiltViewModel(backStackEntry)
                    val viewModel: TaxiReportListViewModelProtocol = viewModelImpl

                    TaxiReportListView(
                        viewModel = viewModel,
                        navController = navController
                    )
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
    isButtonEnabled: Boolean = true,
) {
    val elevationDp by animateDpAsState(
        if (scrollOffset > 0) 4.dp else 0.dp,
        label = "ElevationAnimation"
    )

    TopAppBar(
        title = {
            Row {
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
                    AddButton(
                        contentDescription = "Create Feed",
                        onClick = { navController.navigate(Channel.FeedPostCompose.name) }
                    )
                    HomeViewDropDownMenu(
                        onClickSettings = { navController.navigate(Channel.Settings.name) },
                        onClickNotification = {  }
                    )
                }

                Channel.TimeTable -> {
                    AddButton(
                        contentDescription = "Add Timetable",
                        onClick = {},
                        isEnabled = isButtonEnabled
                    )
                }

                Channel.Taxi -> {
                    if (isButtonEnabled) {
                        AddButton(
                            contentDescription = "Create Taxi Room",
                            onClick = {
                                navController.navigate(Channel.TaxiRoomCreation.name) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    ChatButton(onClick = { navController.navigate(Channel.TaxiChatListView.name) })
                }

                Channel.Boards -> {
                    ChatButton(onClick = { navController.navigate(Channel.AraChatView.name) })
                }

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
    currentScreen: Channel,
) {
    val items = listOf(
        Triple(Channel.Start, stringResource(Channel.Start.title), R.drawable.round_feed),
        Triple(
            Channel.Boards,
            stringResource(Channel.Boards.title),
            R.drawable.round_format_list_bulleted
        ),
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
                } else {
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
private fun Preview() {
    Theme {
        MainTabBar()
    }
}


@Preview
@Composable
private fun AppBarPreview() {
    Theme {
        AppBar(
            currentScreen = Channel.Start
        )
    }
}

@Preview
@Composable
private fun AppDownBarPreview() {
    Theme {
        AppDownBar(
            navController = rememberNavController(),
            currentScreen = Channel.Start
        )
    }
}
