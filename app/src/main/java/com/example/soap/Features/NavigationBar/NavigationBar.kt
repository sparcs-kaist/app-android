package com.example.soap.Features.NavigationBar

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.BoardList.BoardListView
import com.example.soap.Features.Home.HomeView
import com.example.soap.Features.NavigationBar.Animation.trendingEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingExitTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopEnterTransition
import com.example.soap.Features.NavigationBar.Animation.trendingPopExitTransition
import com.example.soap.Features.NavigationBar.Components.NotificationButton
import com.example.soap.Features.NavigationBar.Components.SettingButton
import com.example.soap.Features.PostList.PostListView
import com.example.soap.Features.Timetable.TimetableView
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme

enum class Channel(@StringRes val title: Int) {
    Appname(title = R.string.app_name),
    Start(title = R.string.start),
    TimeTable(title = R.string.timetable),
    Taxi(title = R.string.taxi),
    Trending(title = R.string.trending),
    Boards(title = R.string.board)
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
            ) { HomeView(navController) }

            composable(
                route = Channel.Boards.name,
            ) { BoardListView(navController) }

            composable(
                route = Channel.TimeTable.name,
            ) { TimetableView(navController) }

            composable(
                route = Channel.Taxi.name,
            ) { HomeView(navController) }

            composable(
                route = Channel.Trending.name,
                enterTransition = trendingEnterTransition(),
                exitTransition = trendingExitTransition(),
                popEnterTransition = trendingPopEnterTransition(),
                popExitTransition = trendingPopExitTransition()
            ) { PostListView(navController = navController) }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Channel,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    //Basic Home Navigation Bar
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val alphaValue = collapsedFraction

    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(currentScreen.title),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 12.dp)
            ) {
                Spacer(Modifier.weight(2f))

                Text(
                    text = stringResource(currentScreen.title),
                    style = MaterialTheme.typography.titleLarge,
                    color = (MaterialTheme.colorScheme.onBackground).copy(alpha = alphaValue),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(alignment = Alignment.CenterVertically)
                )

                Spacer(Modifier.weight(1f))

                NotificationButton()

                SettingButton()
            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior
    )
}



@Composable
fun AppDownBar(
    navController: NavController,
    currentScreen: Channel
) {
    val items = listOf(
        Triple(Channel.Start, Channel.Start.name, R.drawable.baseline_home),
        Triple(Channel.Boards, Channel.Boards.name, R.drawable.baseline_topic),
        Triple(Channel.TimeTable, Channel.TimeTable.name, R.drawable.timetable),
        Triple(Channel.Taxi, Channel.Taxi.name, R.drawable.taxi),
        Triple(null, "Search", R.drawable.search)
    )

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
                    label = { Text(label) }
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
                    label = { Text(label) }
                )
            }
        }
    }

}



@Preview
@Composable
private fun Preview(){
    SoapTheme {
        MainTabBar()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AppBarPreview(){
   SoapTheme { AppBar(
       currentScreen = Channel.Start
   ) }
}

@Preview
@Composable
private fun AppDownBarPreview(){
    SoapTheme {
        AppDownBar(
            navController = rememberNavController(),
            currentScreen = Channel.Start
        )
    }
}
