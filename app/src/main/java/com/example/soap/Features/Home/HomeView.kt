package com.example.soap.Features.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.Home.Components.BoardRecentSection
import com.example.soap.Features.Home.Components.BoardsSection
import com.example.soap.Features.Home.Components.TaxiRecentSection
import com.example.soap.Utilities.RoomInfo.Companion.mockList

@Composable
fun HomeView(navController: NavController){
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F6))
            .verticalScroll(scrollState)
    ) {
        BoardRecentSection(title = "Trending")

        TaxiRecentSection(mockList)

        BoardsSection()

        BoardRecentSection(title = "General")

        BoardRecentSection(title = "Notice")

    }
}

@Preview
@Composable
fun Preview(){
    HomeView(rememberNavController())
}
