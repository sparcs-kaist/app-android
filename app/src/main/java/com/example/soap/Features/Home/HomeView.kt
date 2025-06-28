package com.example.soap.Features.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.Home.Components.BoardRecentSection
import com.example.soap.Features.Home.Components.BoardsSection
import com.example.soap.Features.Home.Components.TaxiRecentSection
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.Utilities.Mocks.RoomInfo.Companion.mockList
import com.example.soap.ui.theme.SoapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    navController: NavController,){
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                currentScreen = Channel.Start,
                scrollBehavior = scrollBehavior
            )},

        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), // ✅ Scaffold에 붙이기!!

        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Start
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
            BoardRecentSection(
                title = "Trending",
                clickAction = { navController.navigate(Channel.Trending.name) }
            )
            TaxiRecentSection(mockList)

            BoardsSection()

            BoardRecentSection(
                title = "General",
                clickAction = {})

            BoardRecentSection(
                title = "Notice",
                clickAction = {}
            )
        }
    }
}


@Preview
@Composable
private fun Preview(){
    SoapTheme { HomeView(rememberNavController()) }
}
