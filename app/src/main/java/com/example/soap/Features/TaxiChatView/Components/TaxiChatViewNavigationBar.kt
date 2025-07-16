package com.example.soap.Features.TaxiChatView.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Shared.Mocks.mock
import com.example.soap.ui.theme.SoapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxiChatViewNavigationBar(
    room: TaxiRoom
) {
    TopAppBar(
        title = {
            Column {
                Text(text = room.title)
                Text(
                    text = "${room.source.title} → ${room.destination.title}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { TaxiChatViewNavigationBar(TaxiRoom.mock()) }
}

