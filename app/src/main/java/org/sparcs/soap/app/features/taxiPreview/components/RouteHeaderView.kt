package org.sparcs.soap.app.features.taxiPreview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WhereToVote
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.shared.views.taxiRoomCell.IconText
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun RouteHeaderView(
    source: String,
    destination: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)){
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