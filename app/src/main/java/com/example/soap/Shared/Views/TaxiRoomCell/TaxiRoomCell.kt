package com.example.soap.Shared.Views.TaxiRoomCell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Features.TaxiPreview.Components.RouteHeaderView
import com.example.soap.Shared.Extensions.relativeTimeString
import com.example.soap.Shared.Mocks.mockList
import com.example.soap.Shared.Views.TaxiRoomCell.Components.TaxiParticipantsIndicator
import com.example.soap.ui.theme.Theme

@Composable
fun TaxiRoomCell(
    room: TaxiRoom,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RouteHeaderView(
                        source = room.source.title.localized(),
                        destination = room.destination.title.localized()
                    )
                }

                TaxiParticipantsIndicator(
                    participants = room.participants.size,
                    capacity = room.capacity
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(room.departAt.relativeTimeString(), style = MaterialTheme.typography.bodySmall)
                Text("•", style = MaterialTheme.typography.bodySmall)
                Text(room.title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun IconText(icon: Painter, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { TaxiRoomCell(room = TaxiRoom.mockList()[0], {}) }
}