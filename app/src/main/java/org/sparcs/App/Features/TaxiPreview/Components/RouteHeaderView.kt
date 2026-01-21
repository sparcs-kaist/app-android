package org.sparcs.App.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.App.Shared.Views.TaxiRoomCell.IconText
import org.sparcs.App.theme.ui.Theme

@Composable
fun RouteHeaderView(
    source: String,
    destination: String
) {
    Column{
        IconText(
            icon = Icons.Rounded.NearMe,
            text = source
        )
        IconText(
            icon = Icons.Default.WhereToVote,
            text = destination
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { RouteHeaderView(source = "Seoul", destination = "Busan") }
}