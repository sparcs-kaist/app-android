package com.example.soap.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.R
import com.example.soap.Shared.Views.TaxiRoomCell.IconText
import com.example.soap.ui.theme.SoapTheme

@Composable
fun RouteHeaderView(
    source: String,
    destination: String
) {
    Column{
        IconText(
            icon = painterResource(R.drawable.round_near_me),
            text = source
        )
        IconText(
            icon = painterResource(R.drawable.arrival_point),
            text = destination
        )
    }
}

@Composable
@Preview
private fun Preview() {
    SoapTheme { RouteHeaderView(source = "Seoul", destination = "Busan") }
}