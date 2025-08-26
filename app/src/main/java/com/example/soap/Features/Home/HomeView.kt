package com.example.soap.Features.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.Home.Components.BoardRecentSection
import com.example.soap.Features.Home.Components.BoardsSection
import com.example.soap.Features.Home.Components.TaxiRecentSection
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.R
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.ui.theme.Theme

@Composable
fun HomeView(navController: NavController){
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Start,
                scrollOffset = scrollState.value
            )
                 },
        
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
                title = stringResource(R.string.trending_board),
                clickAction = { navController.navigate(Channel.TrendingBoard.name) }
            )
            TaxiRecentSection(TaxiRoom.mockList())

            BoardsSection()

            BoardRecentSection(
                title = stringResource(R.string.general_board),
                clickAction = {})

            BoardRecentSection(
                title = stringResource(R.string.notice_board),
                clickAction = {}
            )
        }
    }
}


@Preview
@Composable
private fun Preview(){
    Theme { HomeView(rememberNavController()) }
}
