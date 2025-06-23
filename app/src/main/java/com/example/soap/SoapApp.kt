package com.example.soap

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class Channel(@StringRes val title: Int) {
    Appname(title = R.string.app_name),
    Start(title = R.string.start)
}

@Composable
fun SoapApp(navController: NavHostController = rememberNavController()){

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentScreen = Channel.entries.find { screen ->
        currentRoute?.startsWith(screen.name) == true
    }?: Channel.Start

    Scaffold(
        topBar = {
            AppBar(currentScreen = currentScreen)
        },

        bottomBar = {
            AppDownBar(
                    navController = navController,
                    modifier = Modifier,
                    currentScreen = currentScreen
                )
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Channel.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Channel.Start.name) { ListView(navController) }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Channel
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, end = 12.dp)
            ) {
                Text(
                    text = stringResource(currentScreen.title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                NotificationButton()
                Spacer(modifier = Modifier.padding(16.dp))
                MenuButton()
                Spacer(modifier = Modifier.padding(8.dp))

            }
        },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = Color(0xFFF2F2F6)
        )
    )
}


@Composable
fun AppDownBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    currentScreen: Channel
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {

        }

    }
}