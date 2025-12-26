package org.sparcs.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.R
import org.sparcs.ui.theme.Theme
import java.util.Date

@Composable
fun TaxiChatSettlementBubble() {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                    topEnd = 24.dp
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.i_paid_for_the_taxi),
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            painter = painterResource(R.drawable.baseline_credit_card),
            contentDescription = "Credit Card",
            tint = MaterialTheme.colorScheme.background
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        TaxiChatUserWrapper(
            authorID = null,
            authorName = null,
            authorProfileImageURL = null,
            date = Date(),
            isMe = false,
            isGeneral = false,
            isWithdrawn = false,
            badge = true
        ) {
            TaxiChatSettlementBubble()
        }
    }
}
