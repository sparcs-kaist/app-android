package org.sparcs.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.R
import org.sparcs.Shared.Views.TaxiRoomCell.IconText
import org.sparcs.ui.theme.Theme

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
    Theme { RouteHeaderView(source = "Seoul", destination = "Busan") }
}