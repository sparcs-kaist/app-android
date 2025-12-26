package org.sparcs.Shared.Views.TaxiRoomCell.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.R
import org.sparcs.ui.theme.Theme

@Composable
fun TaxiParticipantsIndicator(
    participants: Int,
    capacity: Int
) {
    val accentColor = when (capacity - participants) {
        0 -> Color.Red
        1 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(4.dp)
            .padding(horizontal = 4.dp)
            .background(accentColor.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$participants/$capacity",
            color = accentColor,
            style = MaterialTheme.typography.bodySmall
        )
        Icon(
            painter = painterResource(R.drawable.group),
            contentDescription = "Participants",
            tint = accentColor,
            modifier = Modifier.size(16.dp)
        )
    }
}
@Composable
@Preview
private fun Preview(){
    Theme { TaxiParticipantsIndicator(2, 3) }
}