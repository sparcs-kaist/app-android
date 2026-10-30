package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.theme.ui.Theme

/** Where settlement stands once the taxi has left, as a tonal pill. */
@Composable
fun TaxiRoomStatusIndicator(
    settlementType: TaxiParticipant.SettlementType,
    settlementCount: Int,
    participantsCount: Int
) {
    val scheme = MaterialTheme.colorScheme
    val complete = settlementCount >= participantsCount
    val (container, content) = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) scheme.secondaryContainer to scheme.onSecondaryContainer
            else scheme.primaryContainer to scheme.onPrimaryContainer
        TaxiParticipant.SettlementType.PaymentRequired -> scheme.errorContainer to scheme.onErrorContainer
        TaxiParticipant.SettlementType.PaymentSent -> scheme.secondaryContainer to scheme.onSecondaryContainer
    }
    val text = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) stringResource(R.string.settlement_completed) else stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.PaymentRequired -> stringResource(R.string.payment_required)
        TaxiParticipant.SettlementType.PaymentSent -> stringResource(R.string.payment_settled)
    }
    val icon = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> Icons.Outlined.ErrorOutline
        TaxiParticipant.SettlementType.RequestedSettlement ->
            if (complete) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule
        TaxiParticipant.SettlementType.PaymentRequired -> Icons.Outlined.Payments
        TaxiParticipant.SettlementType.PaymentSent -> Icons.Outlined.CheckCircle
    }
    TaxiTonalLabel(text, container, content, icon = icon)
}

fun TaxiRoom.settlementType(userID: String?): TaxiParticipant.SettlementType =
    participants.firstOrNull { it.id == userID }?.isSettlement ?: TaxiParticipant.SettlementType.NotDeparted

@Composable
fun TaxiRoomStatusIndicator(room: TaxiRoom, userID: String?) {
    TaxiRoomStatusIndicator(
        settlementType = room.settlementType(userID),
        settlementCount = room.settlementTotal ?: 0,
        participantsCount = room.participants.size
    )
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
        }
    }
}