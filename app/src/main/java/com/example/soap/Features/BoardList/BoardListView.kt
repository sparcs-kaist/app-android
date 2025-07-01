package com.example.soap.Features.BoardList

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.BoardList.Components.BoardList
import com.example.soap.Features.BoardList.Components.BoardListSectionItem
import com.example.soap.Features.NavigationBar.AppBar
import com.example.soap.Features.NavigationBar.AppDownBar
import com.example.soap.Features.NavigationBar.Channel
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListView(navController: NavController) {

    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Boards,
                scrollBehavior = scrollBehavior
            )
                 },

        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        bottomBar = {
            AppDownBar(
                navController = navController,
                currentScreen = Channel.Boards
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
            BoardList(
                title = "Notice",
                icon = painterResource(R.drawable.round_notifications_active),
                sections = listOf{
                    BoardListSectionItem(text = "Portal Notice", onClick = {})
                    BoardListSectionItem(text = "Staff Notice", onClick = {})
                    BoardListSectionItem(text = "Facility Notice", onClick = {})
                    BoardListSectionItem(text = "External Company Advertisement", onClick = {})
                }
            )

            BoardList(
                title = "Talk",
                icon = painterResource(R.drawable.round_chat),
                sections = listOf{
                    BoardListSectionItem(text = "General", onClick = {})
                }
            )

            BoardList(
                title = "Organisations and Clubs",
                icon = painterResource(R.drawable.group),
                sections = listOf{
                    BoardListSectionItem(text = "Students Group", onClick = {})
                    BoardListSectionItem(text = "Club", onClick = {})
                }
            )

            BoardList(
                title = "Trade",
                icon = painterResource(R.drawable.baseline_local_offer),
                sections = listOf{
                    BoardListSectionItem(text = "Wanted", onClick = {})
                    BoardListSectionItem(text = "Market", onClick = {})
                    BoardListSectionItem(text = "Real Estate", onClick = {})
                }
            )

            BoardList(
                title = "Communication",
                icon = painterResource(R.drawable.baseline_drafts),
                sections = listOf{
                    BoardListSectionItem(text = "Facility Feedback", onClick = {})
                    BoardListSectionItem(text = "Ara Feedback", onClick = {})
                    BoardListSectionItem(text = "Messages to the School", onClick = {})
                    BoardListSectionItem(text = "KAIST News", onClick = {})
                }
            )
        }
    }
}


@Composable
@Preview
private fun Preview() {
    SoapTheme{ BoardListView(navController = rememberNavController()) }
}