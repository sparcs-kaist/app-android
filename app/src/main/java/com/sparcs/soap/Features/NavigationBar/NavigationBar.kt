package com.sparcs.soap.Features.NavigationBar

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.sparcs.soap.Features.BoardList.BoardListView
import com.sparcs.soap.Features.BoardList.BoardListViewModel
import com.sparcs.soap.Features.Course.CourseView
import com.sparcs.soap.Features.Course.CourseViewModel
import com.sparcs.soap.Features.Course.CourseViewModelProtocol
import com.sparcs.soap.Features.Feed.FeedView
import com.sparcs.soap.Features.Feed.FeedViewModel
import com.sparcs.soap.Features.Feed.FeedViewModelProtocol
import com.sparcs.soap.Features.FeedPost.FeedPostView
import com.sparcs.soap.Features.FeedPost.FeedPostViewModel
import com.sparcs.soap.Features.FeedPost.FeedPostViewModelProtocol
import com.sparcs.soap.Features.FeedPostCompose.FeedPostComposeView
import com.sparcs.soap.Features.FeedPostCompose.FeedPostComposeViewModel
import com.sparcs.soap.Features.FeedPostCompose.FeedPostComposeViewModelProtocol
import com.sparcs.soap.Features.LectureDetail.LectureDetailView
import com.sparcs.soap.Features.LectureDetail.LectureDetailViewModel
import com.sparcs.soap.Features.LectureSearch.LectureSearchViewModel
import com.sparcs.soap.Features.NavigationBar.Animation.trendingEnterTransition
import com.sparcs.soap.Features.NavigationBar.Animation.trendingExitTransition
import com.sparcs.soap.Features.NavigationBar.Animation.trendingPopEnterTransition
import com.sparcs.soap.Features.NavigationBar.Animation.trendingPopExitTransition
import com.sparcs.soap.Features.Post.PostView
import com.sparcs.soap.Features.Post.PostViewModel
import com.sparcs.soap.Features.Post.PostViewModelProtocol
import com.sparcs.soap.Features.PostCompose.PostComposeView
import com.sparcs.soap.Features.PostCompose.PostComposeViewModel
import com.sparcs.soap.Features.PostCompose.PostComposeViewModelProtocol
import com.sparcs.soap.Features.PostList.PostListView
import com.sparcs.soap.Features.PostList.PostListViewModel
import com.sparcs.soap.Features.PostList.PostListViewModelProtocol
import com.sparcs.soap.Features.ReviewCompose.ReviewComposeView
import com.sparcs.soap.Features.ReviewCompose.ReviewComposeViewModel
import com.sparcs.soap.Features.ReviewCompose.ReviewComposeViewModelProtocol
import com.sparcs.soap.Features.Search.SearchView
import com.sparcs.soap.Features.Search.SearchViewModel
import com.sparcs.soap.Features.Search.SearchViewModelProtocol
import com.sparcs.soap.Features.Settings.Ara.AraMyPostView
import com.sparcs.soap.Features.Settings.Ara.AraMyPostViewModel
import com.sparcs.soap.Features.Settings.Ara.AraMyPostViewModelProtocol
import com.sparcs.soap.Features.Settings.Ara.AraSettingsView
import com.sparcs.soap.Features.Settings.Ara.AraSettingsViewModel
import com.sparcs.soap.Features.Settings.Ara.AraSettingsViewModelProtocol
import com.sparcs.soap.Features.Settings.SettingsView
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListView
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModel
import com.sparcs.soap.Features.Settings.Taxi.TaxiReportListViewModelProtocol
import com.sparcs.soap.Features.Settings.Taxi.TaxiSettingsView
import com.sparcs.soap.Features.Settings.Taxi.TaxiSettingsViewModel
import com.sparcs.soap.Features.Settings.Taxi.TaxiSettingsViewModelProtocol
import com.sparcs.soap.Features.SignIn.SignInView
import com.sparcs.soap.Features.SignIn.SignInViewModel
import com.sparcs.soap.Features.TaxiChat.TaxiChatView
import com.sparcs.soap.Features.TaxiChat.TaxiChatViewModel
import com.sparcs.soap.Features.TaxiChat.TaxiChatViewModelProtocol
import com.sparcs.soap.Features.TaxiChatList.TaxiChatListView
import com.sparcs.soap.Features.TaxiChatList.TaxiChatListViewModel
import com.sparcs.soap.Features.TaxiChatList.TaxiChatListViewModelProtocol
import com.sparcs.soap.Features.TaxiList.TaxiListView
import com.sparcs.soap.Features.TaxiList.TaxiListViewModel
import com.sparcs.soap.Features.TaxiList.TaxiListViewModelProtocol
import com.sparcs.soap.Features.TaxiReport.TaxiReportView
import com.sparcs.soap.Features.TaxiReport.TaxiReportViewModel
import com.sparcs.soap.Features.TaxiRoomCreation.TaxiRoomCreationView
import com.sparcs.soap.Features.TaxiRoomCreation.TaxiRoomCreationViewModel
import com.sparcs.soap.Features.Timetable.TimetableView
import com.sparcs.soap.Features.Timetable.TimetableViewModel
import com.sparcs.soap.Features.UserPostList.UserPostListView
import com.sparcs.soap.Features.UserPostList.UserPostListViewModel
import com.sparcs.soap.Features.UserPostList.UserPostListViewModelProtocol
import com.sparcs.soap.R
import com.sparcs.soap.ui.theme.Theme

enum class Channel(@StringRes val title: Int) {
    Appname(title = R.string.app_name),

    //Feed
    Start(title = R.string.start),
    FeedPost(title = R.string.feed_post_view),
    FeedPostCompose(title = R.string.feed_post_compose_view),

    //OTL
    TimeTable(title = R.string.timetable),
    LectureDetail(title = R.string.lecturedetail),
    ReviewCompose(title = R.string.reviewcompose),
    CourseView(title = R.string.course_view),
    LectureSearch(title = R.string.lecture_search_view),

    //Ara
    BoardList(title = R.string.general_board),
    Boards(title = R.string.boards),
    PostView(title = R.string.postview),
    PostCompose(title = R.string.postcompose),
    AraChatView(title = R.string.ara_chat_view), //임시
    UserPostListView(title = R.string.user_post_list_view),

    //Taxi
    Taxi(title = R.string.taxi),
    TaxiRoomCreation(title = R.string.taxi_room_creation),
    TaxiChatView(title = R.string.taxichatview),
    TaxiChatListView(title = R.string.taxichatlistview),
    TaxiReportView(title = R.string.taxi_report_view),

    //Search
    SearchView(title = R.string.search),

    //Setting
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
                route = Channel.FeedPost.name + "?feedId={feedId}",
                arguments = listOf(
                    navArgument("feedId") { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://feedpost?feedId={feedId}"
                        action = Intent.ACTION_VIEW
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

            /*___________OTL___________*/
            navigation(
                startDestination = Channel.TimeTable.name,
                route = "OTLGraph"
            ) {
                composable(
                    route = Channel.TimeTable.name
                ) { backStackEntry ->
                    val viewModelImpl: TimetableViewModel = hiltViewModel(backStackEntry)
                    val viewModel: LectureSearchViewModel = hiltViewModel(backStackEntry)
                    TimetableView(
                        viewModel = viewModelImpl,
                        navController = navController,
                        lectureSearchViewModel = viewModel
                    )
                }

                composable(
                    route = Channel.LectureDetail.name + "?lecture_json={lecture_json}",
                    arguments = listOf(
                        navArgument("lecture_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = trendingPopEnterTransition(),
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val lectureDetailViewModelImpl: LectureDetailViewModel =
                        hiltViewModel(backStackEntry)
                    val timetableViewModelImpl: TimetableViewModel = hiltViewModel(backStackEntry)

                    LectureDetailView(
                        lectureDetailViewModel = lectureDetailViewModelImpl,
                        timetableViewModel = timetableViewModelImpl,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.CourseView.name + "?course_json={course_json}",
                    arguments = listOf(
                        navArgument("course_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
                ) { backStackEntry ->
                    val viewModelImpl: CourseViewModel = hiltViewModel(backStackEntry)
                    val viewModel: CourseViewModelProtocol = viewModelImpl
                    CourseView(navController = navController, viewModel = viewModel)
                }

                composable(
                    route = Channel.ReviewCompose.name + "?lecture_json={lecture_json}",
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = trendingPopEnterTransition(),
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModelImpl: ReviewComposeViewModel = hiltViewModel(backStackEntry)
                    val viewModel: ReviewComposeViewModelProtocol = viewModelImpl
                    val lectureDetailViewModel: LectureDetailViewModel =
                        hiltViewModel(backStackEntry)

                    ReviewComposeView(
                        reviewComposeViewModel = viewModel,
                        lectureDetailViewModel = lectureDetailViewModel,
                        navController = navController
                    )
                }
            }

            /*___________Taxi___________*/
            navigation(
                startDestination = Channel.Taxi.name,
                route = "TaxiGraph"
            ) {
                composable(
                    route = Channel.Taxi.name + "?roomId={roomId}",
                    arguments = listOf(
                        navArgument("roomId") {
                            nullable = true
                            type = NavType.StringType
                        }
                    ),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = "https://taxi.dev.sparcs.org/invite/{roomId}"
                            action = Intent.ACTION_VIEW
                        }
                    )
                )
                { backStackEntry ->
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
                    val taxiListViewModel: TaxiListViewModelProtocol =
                        hiltViewModel<TaxiListViewModel>(parentEntry)
                    val taxiRoomCreationViewModel: TaxiRoomCreationViewModel =
                        hiltViewModel<TaxiRoomCreationViewModel>(parentEntry)

                    TaxiRoomCreationView(
                        navController = navController,
                        taxiListViewModel = taxiListViewModel,
                        taxiRoomCreationViewModel = taxiRoomCreationViewModel
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
                    route = Channel.PostView.name + "?postId={postId}",
                    arguments = listOf(
                        navArgument("postId") { type = NavType.IntType }
                    ),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = "https://newara.dev.sparcs.org/post/{postId}"
                            action = Intent.ACTION_VIEW
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
                route = Channel.SearchView.name
            ) { backStackEntry ->
                val viewModelImpl: SearchViewModel = hiltViewModel(backStackEntry)
                val viewModel: SearchViewModelProtocol = viewModelImpl
                SearchView(viewModel = viewModel, navController = navController)
            }

            composable(
                route = Channel.SignOut.name
            ) { backStackEntry ->
                val viewModelImpl: SignInViewModel = hiltViewModel(backStackEntry)
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
        Triple(Channel.SearchView, stringResource(R.string.search), R.drawable.search)
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
private fun AppDownBarPreview() {
    Theme {
        AppDownBar(
            navController = rememberNavController(),
            currentScreen = Channel.Start
        )
    }
}
