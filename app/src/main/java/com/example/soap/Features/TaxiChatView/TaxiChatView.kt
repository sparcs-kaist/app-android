package com.example.soap.Features.TaxiChatView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.TaxiChatView.Components.TaxiChatViewNavigationBar
import com.example.soap.Shared.Mocks.mockList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatScreen(room: TaxiRoom) {
    Scaffold(
        topBar = {
            TaxiChatViewNavigationBar(room)
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("hello")
        }
    }
}

@Preview
@Composable
fun PreviewTaxiChatScreen() {
    val mockRoom = TaxiRoom.mockList()[0]
    TaxiChatScreen(room = mockRoom)
}
