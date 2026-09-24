package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun ChatSettlementBubble(settlementMeta: TaxiChat.SettlementMeta? = null) {
    Column(
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.i_paid_for_the_taxi),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Rounded.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        settlementMeta?.let { meta ->
            Text(stringResource(R.string.taxi_settlement_total, meta.total),
                color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.taxi_settlement_per_person, meta.perPerson),
                color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.taxi_settlement_participants, meta.participantCount),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        ChatSettlementBubble()
    }
}

@Preview(locale = "ko")
@Preview(locale = "en", fontScale = 1.5f)
@Composable
private fun SettlementDetailsPreview() {
    Theme { ChatSettlementBubble(TaxiChat.SettlementMeta(10000, 3333, 3)) }
}
