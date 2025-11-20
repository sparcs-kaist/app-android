package com.sparcs.soap.Shared.Views.TaxiRoomCell.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sparcs.soap.Domain.Models.Taxi.TaxiParticipant
import com.sparcs.soap.ui.theme.Theme

@Composable
fun TaxiRoomStatusIndicator(
    settlementType: TaxiParticipant.SettlementType,
    settlementCount: Int,
    participantsCount: Int
){
    val accentColor = when (settlementType) {
        TaxiParticipant.SettlementType.NotDeparted -> Color(0xFFFF9800)
        TaxiParticipant.SettlementType.RequestedSettlement -> if(settlementCount >= participantsCount) Color(0xFF4CAF50) else Color(0xFF2196F3)
        TaxiParticipant.SettlementType.PaymentRequired -> Color.Red
        TaxiParticipant.SettlementType.PaymentSent -> Color(0xFF4CAF50)
    }

    val text = when(settlementType){
        TaxiParticipant.SettlementType.NotDeparted -> "Settlement Required"
        TaxiParticipant.SettlementType.RequestedSettlement -> if(settlementCount >= participantsCount) "Settlement Completed" else "Settlement Requested"
        TaxiParticipant.SettlementType.PaymentRequired -> "Payment Required"
        TaxiParticipant.SettlementType.PaymentSent -> "Payment Settled"
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
            text = text,
            color = accentColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        Column{
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