package com.example.soap.Features.NavigationBar

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.soap.Features.Home.HomeView
import com.example.soap.Features.NavigationBar.Components.BoardNavigationBar
import com.example.soap.Features.NavigationBar.Components.NavigationButton
import com.example.soap.Features.NavigationBar.Components.NotificationButton
import com.example.soap.Features.NavigationBar.Components.SearchBottomButton
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
    Trending(title = R.string.trending)
}

@Composable
fun NavigationBar(navController: NavHostController = rememberNavController()){

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Channel.entries.find { screen ->
        currentRoute?.startsWith(screen.name) == true
    }?: Channel.Start

    Scaffold(

        modifier = Modifier.background(Color.Transparent),
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,

        topBar = {
            AppBar(
                navController = navController,
                currentScreen = currentScreen
            )
        },

        bottomBar = {
            AppDownBar(
                    navController = navController,
                    currentScreen = currentScreen
                )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Channel.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Channel.Start.name) { HomeView(navController) }
            composable(route = Channel.TimeTable.name) { TimetableView(navController) }
            composable(route = Channel.Taxi.name) { HomeView(navController) }
            composable(route = Channel.Trending.name){ PostListView()  }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavController,
    currentScreen: Channel
) {
    if(currentScreen.name != Channel.Trending.name) {
        //Basic Home Navigation Bar
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
                    Spacer(Modifier.weight(1f))

                    NotificationButton()

                    SettingButton()
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }else{
        //Board Navigation Bar
        BoardNavigationBar(navController = navController)
    }
}


@Composable
fun AppDownBar(
    navController: NavController,
    currentScreen: Channel
) {


    AnimatedVisibility(
        visible = currentScreen.name != Channel.Trending.name,
        enter = fadeIn() + slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            initialOffsetY = { it }
        ),
        exit = fadeOut() + slideOutVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            ),
            targetOffsetY = { it }
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavigationButton(
                        isSelected = currentScreen == Channel.Start,
                        title = "Home",
                        icon = painterResource(R.drawable.baseline_home),
                        onClick = { navController.navigate(Channel.Start.name) }
                    )

                    NavigationButton(
                        isSelected = currentScreen == Channel.TimeTable,
                        title = "Timetable",
                        icon = painterResource(R.drawable.timetable),
                        onClick = { navController.navigate(Channel.TimeTable.name) }
                    )

                    NavigationButton(
                        isSelected = currentScreen == Channel.Taxi,
                        title = "Taxi",
                        icon = painterResource(R.drawable.taxi),
                        onClick = { navController.navigate(Channel.Taxi.name) }
                    )

                    SearchBottomButton()
                }
            }
        }
    }
}


@Preview
@Composable
private fun Preview(){
    SoapTheme {
        NavigationBar()
    }
}


@Preview
@Composable
private fun AppBarPreview(){
   SoapTheme { AppBar(
       navController = rememberNavController(),
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