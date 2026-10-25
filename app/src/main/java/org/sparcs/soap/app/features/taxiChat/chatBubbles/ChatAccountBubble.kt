package org.sparcs.soap.app.features.taxiChat.chatBubbles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.features.taxiChat.components.copyChatText
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The settler's account: tap the row to copy it. Below, the total and your share, and a button to
 * send the payment, which shows what you have done once it no longer applies.
 */
@Composable
fun ChatAccountBubble(
    content: String,
    totalAmount: Int? = null,
    perPersonAmount: Int? = null,
    isCommitPaymentAvailable: Boolean,
    isPayer: Boolean = false,
    markAsSent: () -> Unit,
) {
    val context = LocalContext.current
    ChatCard {
        val parts = content.split(" ", limit = 2)
        if (parts.size == 2) {
            val (bank, accountNumber) = parts
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.settlement).replaceFirstChar { it.titlecase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("$bank $accountNumber", style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = { context.copyChatText("$bank $accountNumber") }) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.copy))
                }
            }
        } else {
            Text(
                stringResource(R.string.account_parse_failed),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (totalAmount != null || perPersonAmount != null) {
            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (totalAmount != null) {
                    Text(
                        text = stringResource(R.string.taxi_settlement_total, totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (perPersonAmount != null) {
                    Text(
                        text = stringResource(R.string.taxi_settlement_individual, perPersonAmount),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        val (label, icon) = when {
            isPayer -> R.string.requested_settlement to Icons.Filled.Check
            isCommitPaymentAvailable -> R.string.send_payment to Icons.Outlined.Payment
            else -> R.string.already_sent to Icons.Filled.Check
        }
        Button(
            onClick = markAsSent,
            enabled = isCommitPaymentAvailable && !isPayer,
            // Rounded rectangles nest inside the card's corners.
            shape = MaterialTheme.shapes.medium,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(label))
        }
    }
}

@Preview
@Composable
private fun PreviewSendPayment() {
    Theme {
        ChatAccountBubble(
            content = "KB국민 90415338958",
            totalAmount = 10000,
            perPersonAmount = 2500,
            isCommitPaymentAvailable = true,
            isPayer = false,
            markAsSent = { println("mark as sent") }
        )
    }
}

@Preview
@Composable
private fun PreviewAlreadySent() {
    Theme {
        ChatAccountBubble(
            content = "KB국민 90415338958",
            totalAmount = 10000,
            perPersonAmount = 2500,
            isCommitPaymentAvailable = false,
            isPayer = false,
            markAsSent = { println("mark as sent") }
        )
    }
}

@Preview
@Composable
private fun PreviewRequestedSettlement() {
    Theme {
        ChatAccountBubble(
            content = "KB국민 90415338958",
            totalAmount = 10000,
            perPersonAmount = 2500,
            isCommitPaymentAvailable = false,
            isPayer = true,
            markAsSent = { println("mark as sent") }
        )
    }
}
