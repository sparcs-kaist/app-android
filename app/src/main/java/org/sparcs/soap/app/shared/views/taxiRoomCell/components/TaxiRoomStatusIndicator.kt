package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.sparcs.soap.app.theme.ui.lastSeatContainer
import org.sparcs.soap.app.theme.ui.onLastSeatContainer
import org.sparcs.soap.app.theme.ui.onSeatsOpenContainer
import org.sparcs.soap.app.theme.ui.seatsOpenContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.theme.ui.Theme

/**
 * Where your settlement for a departed ride stands, as a small tonal pill with an icon: settlement
 * required, requested or completed, or payment required or sent. [height] fixes its height, to sit
 * level with what is beside it; otherwise it takes its own. [compact] shows only the icon, in a
 * circle, for a narrow column; the label is still read out.
 */
@Composable
fun TaxiRoomStatusIndicator(
    settlementType: TaxiParticipant.SettlementType,
    settlementCount: Int,
    participantsCount: Int,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    compact: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val complete = settlementCount >= participantsCount
    val (container, content) = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> scheme.lastSeatContainer to scheme.onLastSeatContainer
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) scheme.seatsOpenContainer to scheme.onSeatsOpenContainer
            else scheme.primaryContainer to scheme.onPrimaryContainer
        TaxiParticipant.SettlementType.PaymentRequired -> scheme.errorContainer to scheme.onErrorContainer
        TaxiParticipant.SettlementType.PaymentSent -> scheme.seatsOpenContainer to scheme.onSeatsOpenContainer
    }
    val icon = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> Icons.Rounded.Error
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule
        TaxiParticipant.SettlementType.PaymentRequired -> Icons.Rounded.Payments
        TaxiParticipant.SettlementType.PaymentSent -> Icons.Rounded.CheckCircle
    }
    val text = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) stringResource(R.string.settlement_completed) else stringResource(R.string.taxi_settlement_requested)
        TaxiParticipant.SettlementType.PaymentRequired -> stringResource(R.string.payment_required)
        TaxiParticipant.SettlementType.PaymentSent -> stringResource(R.string.payment_settled)
    }
    if (compact) {
        Surface(
            shape = CircleShape,
            color = container,
            contentColor = content,
            modifier = modifier
                .size(height ?: 24.dp)
                .clearAndSetSemantics { contentDescription = text },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        return
    }
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .then(if (height != null) Modifier.height(height) else Modifier)
                .padding(horizontal = 8.dp, vertical = if (height == null) 4.dp else 0.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)){
            TaxiRoomStatusIndicator(
                settlementType = TaxiParticipant.SettlementType.NotDeparted,
                settlementCount = 0,
                participantsCount = 4
            )
            TaxiRoomStatusIndicator(
                settlementType = TaxiParticipant.SettlementType.PaymentRequired,
                settlementCount = 0,
                participantsCount = 4
            )
            TaxiRoomStatusIndicator(
                settlementType = TaxiParticipant.SettlementType.PaymentSent,
                settlementCount = 0,
                participantsCount = 4
            )
            TaxiRoomStatusIndicator(
                settlementType = TaxiParticipant.SettlementType.RequestedSettlement,
                settlementCount = 4,
                participantsCount = 4
            )
            TaxiRoomStatusIndicator(
                settlementType = TaxiParticipant.SettlementType.RequestedSettlement,
                settlementCount = 0,
                participantsCount = 4
            )
        }}
}