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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.soap.ui.theme.soapColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListView(navController: NavController) {

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppBar(
                currentScreen = Channel.Boards,
                scrollOffset = scrollState.value
            )
                 },

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
                .background(MaterialTheme.soapColors.surface)
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {
            BoardList(
                title = stringResource(R.string.notice_board),
                icon = painterResource(R.drawable.round_notifications_active),
                sections = listOf(
                    { BoardListSectionItem(text = stringResource(R.string.portal_notice), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.staff_notice), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.facility_notice), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.external_company_advertisement), onClick = {}) }
                )
            )

            BoardList(
                title = stringResource(R.string.talk_board),
                icon = painterResource(R.drawable.round_chat),
                sections = listOf(
                    { BoardListSectionItem(text = stringResource(R.string.talk_board), onClick = {}) }
                )
            )

            BoardList(
                title = stringResource(R.string.organizations_and_clubs),
                icon = painterResource(R.drawable.group),
                sections = listOf(
                    { BoardListSectionItem(text = stringResource(R.string.students_group), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.club), onClick = {}) }
                )
            )

            BoardList(
                title = stringResource(R.string.trade),
                icon = painterResource(R.drawable.baseline_local_offer),
                sections = listOf(
                    { BoardListSectionItem(text = stringResource(R.string.wanted), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.market), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.real_estate), onClick = {}) }
                )
            )

            BoardList(
                title = stringResource(R.string.communication),
                icon = painterResource(R.drawable.baseline_drafts),
                sections = listOf(
                    { BoardListSectionItem(text = stringResource(R.string.facility_feedback), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.ara_feedback), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.messages_to_the_school), onClick = {}) },
                    { BoardListSectionItem(text = stringResource(R.string.kaist_news), onClick = {}) }
                )
            )
        }
    }
}


@Composable
@Preview
private fun Preview() {
    SoapTheme{ BoardListView(navController = rememberNavController()) }
}