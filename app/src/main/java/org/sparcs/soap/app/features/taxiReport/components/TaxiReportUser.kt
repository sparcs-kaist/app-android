package org.sparcs.soap.app.features.taxiReport.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.taxi.TaxiParticipant
import org.sparcs.soap.app.theme.ui.Theme
import java.util.Date
import java.util.UUID

@Composable
fun TaxiReportUser(
    user: TaxiParticipant,
    onClick: () -> Unit,
    isChecked: Boolean
) {
    val statusText = when (user.isSettlement) {
        TaxiParticipant.SettlementType.RequestedSettlement -> stringResource(R.string.request_settlement)
        TaxiParticipant.SettlementType.PaymentSent -> stringResource(R.string.paid)
        TaxiParticipant.SettlementType.PaymentRequired -> stringResource(R.string.not_paid)
        else -> null
    }
    ListItem(
        leadingContent = { UserProfileImage(user = user) },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(user.nickname)
                if (user.badge) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = "Badge",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        supportingContent = statusText?.let { { Text(it) } },
        trailingContent = { RadioButton(selected = isChecked, onClick = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.selectable(selected = isChecked, role = Role.RadioButton, onClick = onClick)
    )
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
            readAt = Date(),
            hasCarrier = false,
            isArrived = false
        ), {}, false
        )
    }
}
