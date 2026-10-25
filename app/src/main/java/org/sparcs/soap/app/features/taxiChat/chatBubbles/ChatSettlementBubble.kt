package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/**
 * "I paid for the taxi" and, when given, the total and each person's share, coloured like a text
 * bubble from the same sender.
 */
@Composable
fun ChatSettlementBubble(settlementMeta: TaxiChat.SettlementMeta? = null, isMine: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(text = stringResource(R.string.i_paid_for_the_taxi), style = MaterialTheme.typography.bodyLarge)
            }
            settlementMeta?.let { meta ->
                Text(stringResource(R.string.taxi_settlement_total, meta.total), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.taxi_settlement_per_person, meta.perPerson), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.taxi_settlement_participants, meta.participantCount),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        ChatSettlementBubble(isMine = true)
    }
}

@Preview(locale = "ko")
@Preview(locale = "en", fontScale = 1.5f)
@Composable
private fun SettlementDetailsPreview() {
    Theme { ChatSettlementBubble(TaxiChat.SettlementMeta(10000, 3333, 3)) }
}
