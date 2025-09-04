package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.NavigationBar.Components.DismissButton
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom,
    onDismiss: () -> Unit,
    onClickCallTaxi: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = { DismissButton(onClick = { onDismiss() }) },
        title = {
            Column {
                Text(text = room.title, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = "${room.source.title} → ${room.destination.title}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        actions = {
            TaxiChatViewDropDownMenu(
                room = room,
                onClickShare = { /* TODO: Share room */ },
                onClickCallTaxi = { onClickCallTaxi() },
                onClickReport = { /* TODO: Report room */ }
            ) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}



@Composable
@Preview
private fun Preview(){
    Theme {
        Box(Modifier.fillMaxSize()){ TaxiChatViewNavigationBar(TaxiRoom.mock(), {}, {}) } }
}

