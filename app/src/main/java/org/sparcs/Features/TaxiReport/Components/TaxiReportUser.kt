package org.sparcs.Features.TaxiReport.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.R
import org.sparcs.ui.theme.Theme
import java.util.Date
import java.util.UUID

@Composable
fun TaxiReportUser(
    user: TaxiParticipant,
    onClick: () -> Unit,
    isChecked: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        UserProfileImage(user = user)
        Spacer(Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.width(4.dp))
                if(user.badge){
                    Icon(
                        painter = painterResource(R.drawable.phone_circle_fill),
                        contentDescription = "Badge",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            if (user.isSettlement != TaxiParticipant.SettlementType.NotDeparted) {
                val statusText = when (user.isSettlement) {
                    TaxiParticipant.SettlementType.RequestedSettlement -> stringResource(R.string.request_settlement)
                    TaxiParticipant.SettlementType.PaymentSent -> stringResource(R.string.paid)
                    TaxiParticipant.SettlementType.PaymentRequired -> stringResource(R.string.not_paid)
                    else -> ""
                }
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (isChecked) {
            Icon(
                painter = painterResource(R.drawable.round_check),
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun UserProfileImage(user: TaxiParticipant) {
    val imageModifier = Modifier
        .size(32.dp)
        .clip(CircleShape)

    if (user.profileImageURL != null) {
        AsyncImage(
            model = user.profileImageURL.toString(),
            contentDescription = null,
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = imageModifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
@Preview
private fun Preview(){
    Theme {
        TaxiReportUser(user =  TaxiParticipant(
            id = UUID.randomUUID().toString(),
            name = "name",
            nickname = "nickname",
            profileImageURL = null,
            withdraw = false,
            badge = true,
            isSettlement = TaxiParticipant.SettlementType.PaymentRequired,
            readAt = Date()
        ), {}, false
        )
    }
}
