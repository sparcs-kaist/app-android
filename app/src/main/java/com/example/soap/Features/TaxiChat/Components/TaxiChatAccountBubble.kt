package com.example.soap.Features.TaxiChat.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import java.util.Date

@Composable
fun TaxiChatAccountBubble(
    content: String,
    isCommitPaymentAvailable: Boolean,
    markAsSent: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "settlement".uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        val parts = content.split(" ", limit = 2)
        if (parts.size == 2) {
            val bank = parts[0]
            val accountNumber = parts[1]
            // use bank and accountNumber here

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.combinedClickable (
                    onClick = { clipboardManager.setText(AnnotatedString("$bank $accountNumber")) },
                    onLongClick = {
                        clipboardManager.setText(AnnotatedString("$bank $accountNumber"))
                    }
                )) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(bank, fontWeight = FontWeight.SemiBold)
                Text(accountNumber)
            }
        } else {
            Text("Failed to parse account information.")
        }

        Button (
            onClick = { markAsSent() },
            enabled = isCommitPaymentAvailable,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(isCommitPaymentAvailable){
                Icon(painterResource(R.drawable.round_payment), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Send Payment",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Icon(painterResource(R.drawable.round_check), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Already Sent",
                    fontWeight = FontWeight.Medium
                )
            }

        }
    }
}

@Preview
@Composable
private fun Preview() {
    TaxiChatUserWrapper(
        authorID = null,
        authorName = null,
        authorProfileImageURL = null,
        date = Date(),
        isMe = false,
        isGeneral = false,
        isWithdrawn = false
    ) {
        TaxiChatAccountBubble(
            content = "KB국민 90415338958",
            isCommitPaymentAvailable = false,
            markAsSent = { println("mark as sent") }
        )
    }
}
