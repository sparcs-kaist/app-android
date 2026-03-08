package org.sparcs.soap.App.Features.TaxiChat.ChatBubbles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.R

@Composable
fun ChatGeneralMessage(
    authorName: String?,
    type: TaxiChat.ChatType,
) {
    val displayName = authorName ?: stringResource(R.string.unknown)
    when (type) {
        TaxiChat.ChatType.ENTRANCE -> {
            Box(
                Modifier.fillMaxWidth().padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.has_joined, displayName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
        TaxiChat.ChatType.EXIT -> {
            Box(
                Modifier.fillMaxWidth().padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.has_left, displayName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
        else -> {}
    }
}
//
//@Preview
//@Composable
//private fun Preview() {
//    Column {
//        ChatGeneralMessage("testuser", TaxiChat.ChatType.IN)
//        ChatGeneralMessage("testuser", TaxiChat.ChatType.OUT)
//    }
//}
