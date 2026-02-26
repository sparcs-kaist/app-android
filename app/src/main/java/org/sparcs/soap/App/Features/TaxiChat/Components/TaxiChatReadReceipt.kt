package org.sparcs.soap.App.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.App.theme.ui.Theme

@Composable
fun TaxiChatReadReceipt(
    readCount: Int,
    showTimeLabel: Boolean,
    time: String,
    alignment: Alignment.Horizontal,
) {
    Column(
        horizontalAlignment = alignment
    ) {
        if (readCount > 0) {
            Text(
                text = readCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (showTimeLabel) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme {
        Column(horizontalAlignment = Alignment.End) {
            TaxiChatReadReceipt(
                readCount = 1,
                showTimeLabel = true,
                time = "오후 2:30",
                alignment = Alignment.End
            )

            TaxiChatReadReceipt(
                readCount = 0,
                showTimeLabel = true,
                time = "오후 2:31",
                alignment = Alignment.Start,
            )
        }
    }
}