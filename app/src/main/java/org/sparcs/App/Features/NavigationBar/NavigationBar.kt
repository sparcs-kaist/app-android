package org.sparcs.App.Features.NavigationBar

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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import org.sparcs.App.Domain.Helpers.Constants
import org.sparcs.App.Features.BoardList.BoardListView
import org.sparcs.App.Features.BoardList.BoardListViewModel
import org.sparcs.App.Features.Course.CourseView
import org.sparcs.App.Features.Course.CourseViewModel
import org.sparcs.App.Features.Feed.FeedView
import org.sparcs.App.Features.Feed.FeedViewModel
import org.sparcs.App.Features.FeedPost.FeedPostView
import org.sparcs.App.Features.FeedPost.FeedPostViewModel
import org.sparcs.App.Features.FeedPost.FeedPostViewModelProtocol
import org.sparcs.App.Features.FeedPostCompose.FeedPostComposeView
import org.sparcs.App.Features.FeedPostCompose.FeedPostComposeViewModel
import org.sparcs.App.Features.LectureDetail.LectureDetailView
import org.sparcs.App.Features.LectureDetail.LectureDetailViewModel
import org.sparcs.App.Features.LectureSearch.LectureSearchViewModel
import org.sparcs.App.Features.NavigationBar.Animation.trendingEnterTransition
import org.sparcs.App.Features.NavigationBar.Animation.trendingExitTransition
import org.sparcs.App.Features.NavigationBar.Animation.trendingPopExitTransition
import org.sparcs.App.Features.Post.PostView
import org.sparcs.App.Features.Post.PostViewModel
import org.sparcs.App.Features.PostCompose.PostComposeView
import org.sparcs.App.Features.PostCompose.PostComposeViewModel
import org.sparcs.App.Features.PostList.PostListView
import org.sparcs.App.Features.PostList.PostListViewModel
import org.sparcs.App.Features.ReviewCompose.ReviewComposeView
import org.sparcs.App.Features.ReviewCompose.ReviewComposeViewModel
import org.sparcs.App.Features.Search.SearchView
import org.sparcs.App.Features.Search.SearchViewModel
import org.sparcs.App.Features.Settings.Ara.AraMyPostView
import org.sparcs.App.Features.Settings.Ara.AraMyPostViewModel
import org.sparcs.App.Features.Settings.Ara.AraSettingsView
import org.sparcs.App.Features.Settings.Ara.AraSettingsViewModel
import org.sparcs.App.Features.Settings.Feed.FeedSettingsView
import org.sparcs.App.Features.Settings.Feed.FeedSettingsViewModel
import org.sparcs.App.Features.Settings.SettingsView
import org.sparcs.App.Features.Settings.SettingsViewModel
import org.sparcs.App.Features.Settings.Taxi.TaxiReportListView
import org.sparcs.App.Features.Settings.Taxi.TaxiReportListViewModel
import org.sparcs.App.Features.Settings.Taxi.TaxiSettingsView
import org.sparcs.App.Features.Settings.Taxi.TaxiSettingsViewModel
import org.sparcs.App.Features.SignIn.SignInView
import org.sparcs.App.Features.SignIn.SignInViewModel
import org.sparcs.App.Features.TaxiChat.TaxiChatView
import org.sparcs.App.Features.TaxiChat.TaxiChatViewModel
import org.sparcs.App.Features.TaxiChatList.TaxiChatListView
import org.sparcs.App.Features.TaxiChatList.TaxiChatListViewModel
import org.sparcs.App.Features.TaxiList.TaxiListView
import org.sparcs.App.Features.TaxiList.TaxiListViewModel
import org.sparcs.App.Features.TaxiList.TaxiListViewModelProtocol
import org.sparcs.App.Features.TaxiPreview.TaxiPreviewViewModel
import org.sparcs.App.Features.TaxiReport.TaxiReportView
import org.sparcs.App.Features.TaxiReport.TaxiReportViewModel
import org.sparcs.App.Features.TaxiRoomCreation.TaxiRoomCreationView
import org.sparcs.App.Features.TaxiRoomCreation.TaxiRoomCreationViewModel
import org.sparcs.App.Features.Timetable.TimetableView
import org.sparcs.App.Features.Timetable.TimetableViewModel
import org.sparcs.App.Features.UserPostList.UserPostListView
import org.sparcs.App.Features.UserPostList.UserPostListViewModel
import org.sparcs.App.theme.ui.Theme
import org.sparcs.R

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
    FeedSettings(title = R.string.feed_settings),
    AraSettings(title = R.string.ara_settings),
    AraMyPostSettings(title = R.string.ara_my_post_settings),
    TaxiSettings(title = R.string.taxi_settings),
    TaxiReportSettings(title = R.string.taxi_report_settings)
}

@Composable
fun MainTabBar(navController: NavHostController = rememberNavController()) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        NavHost(
            navController = navController,
            startDestination = "FeedGraph",
            enterTransition = { fadeIn(animationSpec = tween(500)) },
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            navigation(
                startDestination = Channel.Start.name,
                route = "FeedGraph"
            ) {
                composable(
                    route = Channel.Start.name,
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("FeedGraph")
                    }
                    val viewModel: FeedViewModel = hiltViewModel(parentEntry)
                    FeedView(navController = navController, viewModel = viewModel)
                }

                composable(
                    route = Channel.FeedPost.name + "?feedId={feedId}",
                    arguments = listOf(
                        navArgument("feedId") { type = NavType.StringType }
                    ),
                    deepLinks = listOf(
                        navDeepLink {
                            uriPattern = Constants.feedShareURL + "{feedId}"
                            action = Intent.ACTION_VIEW
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: FeedPostViewModelProtocol = hiltViewModel<FeedPostViewModel>(backStackEntry)
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("FeedGraph")
                    }
                    val feedViewModel: FeedViewModel = hiltViewModel(parentEntry)
                    FeedPostView(
                        navController = navController,
                        viewModel = viewModel,
                        feedViewModel = feedViewModel
                    )
                }

                composable(
                    route = Channel.FeedPostCompose.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: FeedPostComposeViewModel = hiltViewModel(backStackEntry)
                    FeedPostComposeView(navController = navController, viewModel = viewModel)
                }
            }

            /*___________OTL___________*/
            navigation(
                startDestination = Channel.TimeTable.name,
                route = "OTLGraph"
            ) {
                composable(
                    route = Channel.TimeTable.name
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("OTLGraph")
                    }
                    val viewModel: TimetableViewModel = hiltViewModel(parentEntry)
                    val lectureSearchViewModel: LectureSearchViewModel =
                        hiltViewModel(backStackEntry)
                    TimetableView(
                        viewModel = viewModel,
                        navController = navController,
                        lectureSearchViewModel = lectureSearchViewModel
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
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("OTLGraph")
                    }

                    val timetableViewModel: TimetableViewModel = hiltViewModel(parentEntry)
                    val lectureDetailViewModel: LectureDetailViewModel =
                        hiltViewModel(backStackEntry)
//                    val timetableViewModel: TimetableViewModel = hiltViewModel(backStackEntry)

                    LectureDetailView(
                        lectureDetailViewModel = lectureDetailViewModel,
                        timetableViewModel = timetableViewModel,
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
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: CourseViewModel = hiltViewModel(backStackEntry)
                    CourseView(navController = navController, viewModel = viewModel)
                }

                composable(
                    route = Channel.ReviewCompose.name + "?lecture_json={lecture_json}",
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: ReviewComposeViewModel = hiltViewModel(backStackEntry)
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
                            uriPattern = Constants.taxiInviteURL + "{roomId}"
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

                    val taxiPreviewViewModel: TaxiPreviewViewModel = hiltViewModel(backStackEntry)

                    TaxiListView(
                        viewModel = viewModel,
                        taxiPreviewViewModel = taxiPreviewViewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiRoomCreation.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("TaxiGraph")
                    }
                    val taxiListViewModel: TaxiListViewModelProtocol =
                        hiltViewModel<TaxiListViewModel>(parentEntry)
                    val taxiRoomCreationViewModel: TaxiRoomCreationViewModel =
                        hiltViewModel(backStackEntry)

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
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: TaxiChatViewModel = hiltViewModel(backStackEntry)
                    TaxiChatView(
                        viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiChatListView.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: TaxiChatListViewModel = hiltViewModel(backStackEntry)
                    TaxiChatListView(viewModel, navController)
                }

                composable(
                    route = Channel.TaxiReportView.name + "?room_json={room_json}",
                    arguments = listOf(
                        navArgument("room_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: TaxiReportViewModel = hiltViewModel(backStackEntry)
                    TaxiReportView(viewModel, navController)
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
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: PostListViewModel = hiltViewModel(backStackEntry)
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
                            uriPattern = Constants.araShareURL + "{postId}"
                            action = Intent.ACTION_VIEW
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: PostViewModel = hiltViewModel(backStackEntry)
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
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: PostComposeViewModel = hiltViewModel(backStackEntry)
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
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: UserPostListViewModel = hiltViewModel(backStackEntry)
                    UserPostListView(viewModel = viewModel, navController = navController)
                }
            }

            composable(
                route = Channel.SearchView.name
            ) { backStackEntry ->
                val viewModel: SearchViewModel = hiltViewModel(backStackEntry)
                val taxiPreviewViewModel: TaxiPreviewViewModel = hiltViewModel(backStackEntry)
                SearchView(
                    viewModel = viewModel,
                    taxiPreviewViewModel = taxiPreviewViewModel,
                    navController = navController
                )
            }

            composable(
                route = Channel.SignOut.name
            ) { backStackEntry ->
                val viewModel: SignInViewModel = hiltViewModel(backStackEntry)
                SignInView(viewModel = viewModel)
            }

            /*___________Settings___________*/
            navigation(
                startDestination = Channel.Settings.name,
                route = "SettingsGraph"
            ) {
                composable(
                    route = Channel.Settings.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: SettingsViewModel = hiltViewModel(backStackEntry)
                    SettingsView(navController = navController, settingsViewModel = viewModel)
                }

                composable(
                    route = Channel.AraSettings.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: AraSettingsViewModel = hiltViewModel(backStackEntry)
                    AraSettingsView(viewModel = viewModel, navController = navController)
                }

                composable(
                    route = Channel.FeedSettings.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: FeedSettingsViewModel = hiltViewModel(backStackEntry)
                    FeedSettingsView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.AraMyPostSettings.name + "?type_json={type_json}",
                    arguments = listOf(
                        navArgument("type_json") {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: AraMyPostViewModel = hiltViewModel(backStackEntry)
                    AraMyPostView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiSettings.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: TaxiSettingsViewModel = hiltViewModel(backStackEntry)
                    TaxiSettingsView(
                        viewModel = viewModel,
                        navController = navController
                    )
                }

                composable(
                    route = Channel.TaxiReportSettings.name,
                    enterTransition = trendingEnterTransition(),
                    exitTransition = trendingExitTransition(),
                    popEnterTransition = null,
                    popExitTransition = trendingPopExitTransition()
                ) { backStackEntry ->
                    val viewModel: TaxiReportListViewModel = hiltViewModel(backStackEntry)
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
                    colors = NavigationBarItemDefaults.colors(
                        MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    Theme {
        MainTabBar(rememberNavController())
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
