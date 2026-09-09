package org.sparcs.soap.app.features.navigationBar

import android.content.Intent
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Feed
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.LocalTaxi
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.features.boardList.BoardListView
import org.sparcs.soap.app.features.boardList.BoardListViewModel
import org.sparcs.soap.app.features.course.CourseView
import org.sparcs.soap.app.features.course.CourseViewModel
import org.sparcs.soap.app.features.credit.CreditView
import org.sparcs.soap.app.features.feed.FeedView
import org.sparcs.soap.app.features.feed.FeedViewModel
import org.sparcs.soap.app.features.feedPost.FeedPostView
import org.sparcs.soap.app.features.feedPost.FeedPostViewModel
import org.sparcs.soap.app.features.feedPost.FeedPostViewModelProtocol
import org.sparcs.soap.app.features.feedPostCompose.FeedPostComposeView
import org.sparcs.soap.app.features.feedPostCompose.FeedPostComposeViewModel
import org.sparcs.soap.app.features.lectureDetail.LectureDetailView
import org.sparcs.soap.app.features.lectureDetail.LectureDetailViewModel
import org.sparcs.soap.app.features.lectureSearch.LectureSearchViewModel
import org.sparcs.soap.app.features.navigationBar.animation.trendingEnterTransition
import org.sparcs.soap.app.features.navigationBar.animation.trendingExitTransition
import org.sparcs.soap.app.features.navigationBar.animation.trendingPopExitTransition
import org.sparcs.soap.app.features.navigationBar.components.MainDeepLinkHandler
import org.sparcs.soap.app.features.post.PostView
import org.sparcs.soap.app.features.post.PostViewModel
import org.sparcs.soap.app.features.postCompose.PostComposeView
import org.sparcs.soap.app.features.postCompose.PostComposeViewModel
import org.sparcs.soap.app.features.postList.PostListView
import org.sparcs.soap.app.features.postList.PostListViewModel
import org.sparcs.soap.app.features.reviewCompose.ReviewComposeView
import org.sparcs.soap.app.features.reviewCompose.ReviewComposeViewModel
import org.sparcs.soap.app.features.search.SearchView
import org.sparcs.soap.app.features.search.SearchViewModel
import org.sparcs.soap.app.features.settings.SettingsView
import org.sparcs.soap.app.features.settings.SettingsViewModel
import org.sparcs.soap.app.features.settings.ara.AraMyPostView
import org.sparcs.soap.app.features.settings.ara.AraMyPostViewModel
import org.sparcs.soap.app.features.settings.ara.AraSettingsView
import org.sparcs.soap.app.features.settings.ara.AraSettingsViewModel
import org.sparcs.soap.app.features.settings.feed.FeedSettingsView
import org.sparcs.soap.app.features.settings.feed.FeedSettingsViewModel
import org.sparcs.soap.app.features.settings.notification.NotificationSettingsView
import org.sparcs.soap.app.features.settings.notification.NotificationSettingsViewModel
import org.sparcs.soap.app.features.settings.taxi.TaxiReportListView
import org.sparcs.soap.app.features.settings.taxi.TaxiReportListViewModel
import org.sparcs.soap.app.features.settings.taxi.TaxiSettingsView
import org.sparcs.soap.app.features.settings.taxi.TaxiSettingsViewModel
import org.sparcs.soap.app.features.signIn.SignInView
import org.sparcs.soap.app.features.signIn.SignInViewModel
import org.sparcs.soap.app.features.taxiChat.TaxiChatView
import org.sparcs.soap.app.features.taxiChat.TaxiChatViewModel
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListView
import org.sparcs.soap.app.features.taxiChatList.TaxiChatListViewModel
import org.sparcs.soap.app.features.taxiList.TaxiListView
import org.sparcs.soap.app.features.taxiList.TaxiListViewModel
import org.sparcs.soap.app.features.taxiList.TaxiListViewModelProtocol
import org.sparcs.soap.app.features.taxiPreview.TaxiPreviewViewModel
import org.sparcs.soap.app.features.taxiReport.TaxiReportView
import org.sparcs.soap.app.features.taxiReport.TaxiReportViewModel
import org.sparcs.soap.app.features.taxiRoomCreation.TaxiRoomCreationView
import org.sparcs.soap.app.features.taxiRoomCreation.TaxiRoomCreationViewModel
import org.sparcs.soap.app.features.timetable.TimetableView
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.features.userPostList.UserPostListView
import org.sparcs.soap.app.features.userPostList.UserPostListViewModel
import org.sparcs.soap.app.theme.ui.Theme

enum class Channel(@param:StringRes val title: Int) {
    //Feed
    Start(title = R.string.start),
    FeedPost(title = R.string.feed_post_view),
    FeedPostCompose(title = R.string.feed_post_compose_view),

    //OTL
    TimeTable(title = R.string.timetable),
    LectureDetail(title = R.string.lecturedetail),
    ReviewCompose(title = R.string.reviewcompose),
    CourseView(title = R.string.course_view),

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
    NotificationSettings(title = R.string.notifications_title),
    CreditView(title = R.string.acknowledgements),
    FeedSettings(title = R.string.feed_settings),
    AraSettings(title = R.string.ara_settings),
    AraMyPostSettings(title = R.string.ara_my_post_settings),
    TaxiSettings(title = R.string.taxi_settings),
    TaxiReportSettings(title = R.string.taxi_report_settings)
}

@Composable
fun MainTabBar(navController: NavHostController = rememberNavController()) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val navBackStackEntryState = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntryState.value?.destination?.route

    val navigationItems = listOf(
        Triple(Channel.Start, stringResource(Channel.Start.title), Icons.AutoMirrored.Rounded.Feed),
        Triple(
            Channel.Boards,
            stringResource(Channel.Boards.title),
            Icons.AutoMirrored.Rounded.FormatListBulleted
        ),
        Triple(
            Channel.TimeTable,
            stringResource(Channel.TimeTable.title),
            Icons.Rounded.TableChart
        ),
        Triple(Channel.Taxi, stringResource(Channel.Taxi.title), Icons.Rounded.LocalTaxi),
        Triple(Channel.SearchView, stringResource(R.string.search), Icons.Rounded.Search)
    )

    fun isTabActive(route: String?, target: Channel): Boolean {
        if (route == null) return false
        val base = route.substringBefore('?')
        return base == target.name
    }

    val isMainTab = when {
        isTabActive(currentRoute, Channel.Start) -> true
        isTabActive(currentRoute, Channel.Boards) -> true
        isTabActive(currentRoute, Channel.TimeTable) -> true
        isTabActive(currentRoute, Channel.Taxi) -> true
        isTabActive(currentRoute, Channel.SearchView) -> true
        else -> false
    }

    val onTabClick: (Channel) -> Unit = { channel ->
        if (!isTabActive(currentRoute, channel)) {
            val targetRoute = when (channel) {
                Channel.Start -> "FeedGraph"
                Channel.Boards -> "AraGraph"
                Channel.TimeTable -> "OTLGraph"
                Channel.Taxi -> "TaxiGraph"
                Channel.SearchView -> Channel.SearchView.name
                else -> channel.name
            }
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        bottomBar = {
            if (!isLandscape) {
                AnimatedVisibility(
                    visible = isMainTab,
                    enter = slideInVertically(animationSpec = tween(150), initialOffsetY = { it }),
                    exit = slideOutVertically(animationSpec = tween(150), targetOffsetY = { it })
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        navigationItems.forEach { (channel, label, icon) ->
                            NavigationBarItem(
                                selected = isTabActive(currentRoute, channel),
                                onClick = { onTabClick(channel) },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            if (isLandscape) {
                AnimatedVisibility(
                    visible = isMainTab,
                    enter = slideInHorizontally(
                        tween(150),
                        initialOffsetX = { -it }) + fadeIn(tween(150)),
                    exit = slideOutHorizontally(
                        tween(150),
                        targetOffsetX = { -it }) + fadeOut(tween(150))
                ) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        header = {
                            Spacer(Modifier.padding(top = 8.dp))
                        }
                    ) {
                        navigationItems.forEach { (channel, label, icon) ->
                            NavigationRailItem(
                                selected = isTabActive(currentRoute, channel),
                                onClick = { onTabClick(channel) },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                alwaysShowLabel = false,
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "FeedGraph"
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
                                    uriPattern = Constants.FEED_SHARE_URL + "{feedId}"
                                    action = Intent.ACTION_VIEW
                                }
                            ),
                            enterTransition = trendingEnterTransition(),
                            exitTransition = trendingExitTransition(),
                            popEnterTransition = null,
                            popExitTransition = trendingPopExitTransition()
                        ) { backStackEntry ->
                            val viewModel: FeedPostViewModelProtocol =
                                hiltViewModel<FeedPostViewModel>(backStackEntry)
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
                            FeedPostComposeView(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }

                    /*___________OTL___________*/
                    navigation(
                        startDestination = Channel.TimeTable.name,
                        route = "OTLGraph"
                    ) {
                        composable(
                            route = Channel.TimeTable.name,
                            deepLinks = listOf(navDeepLink {
                                uriPattern = Constants.OTL_SHARE_URL
                                action = Intent.ACTION_VIEW
                            })
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
                                },
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

                            LectureDetailView(
                                viewModel = lectureDetailViewModel,
                                timetableViewModel = timetableViewModel,
                                navController = navController
                            )
                        }

                        composable(
                            route = Channel.CourseView.name + "?courseId={courseId}",
                            arguments = listOf(
                                navArgument("courseId") {
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
                            route = Channel.ReviewCompose.name + "?lecture_json={lecture_json}&written_review_json={written_review_json}",
                            arguments = listOf(
                                navArgument("lecture_json") {
                                    type = NavType.StringType
                                    nullable = false
                                },
                                navArgument("written_review_json") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            ),
                            enterTransition = trendingEnterTransition(),
                            exitTransition = trendingExitTransition(),
                            popEnterTransition = null,
                            popExitTransition = trendingPopExitTransition()
                        ) { backStackEntry ->
                            val viewModel: ReviewComposeViewModel = hiltViewModel(backStackEntry)
                            val parentEntry = remember(backStackEntry) {
                                navController.getBackStackEntry(Channel.LectureDetail.name + "?lecture_json={lecture_json}")
                            }
                            val lectureDetailViewModel: LectureDetailViewModel =
                                hiltViewModel(parentEntry)

                            ReviewComposeView(
                                viewModel = viewModel,
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
                                    uriPattern = Constants.TAXI_INVITE_URL + "{roomId}"
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

                            val taxiPreviewViewModel: TaxiPreviewViewModel =
                                hiltViewModel(backStackEntry)

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
                        startDestination = "AraBoardList",
                        route = "AraGraph"
                    ) {
                        navigation(
                            startDestination = Channel.Boards.name,
                            route = "AraBoardList"
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
                        }

                        composable(
                            route = Channel.PostView.name + "?postId={postId}",
                            arguments = listOf(
                                navArgument("postId") { type = NavType.IntType }
                            ),
                            deepLinks = listOf(
                                navDeepLink {
                                    uriPattern = Constants.ARA_SHARE_URL + "{postId}"
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
                        val taxiPreviewViewModel: TaxiPreviewViewModel =
                            hiltViewModel(backStackEntry)
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
                            SettingsView(
                                navController = navController,
                                settingsViewModel = viewModel
                            )
                        }

                        composable(
                            route = Channel.NotificationSettings.name,
                            enterTransition = trendingEnterTransition(),
                            exitTransition = trendingExitTransition(),
                            popEnterTransition = null,
                            popExitTransition = trendingPopExitTransition()
                        ) { backStackEntry ->
                            val viewModel: NotificationSettingsViewModel =
                                hiltViewModel(backStackEntry)
                            NotificationSettingsView(navController, viewModel)
                        }

                        composable(
                            route = Channel.CreditView.name,
                            enterTransition = trendingEnterTransition(),
                            exitTransition = trendingExitTransition(),
                            popEnterTransition = null,
                            popExitTransition = trendingPopExitTransition()
                        ) {
                            CreditView(navController = navController)
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
                MainDeepLinkHandler(
                    navController = navController,
                    onTabSelected = onTabClick
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
