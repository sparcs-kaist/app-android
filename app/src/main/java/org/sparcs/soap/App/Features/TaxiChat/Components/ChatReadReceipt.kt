package org.sparcs.soap.App.Features.TaxiChat.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.App.Shared.Extensions.formattedTime
import org.sparcs.soap.App.theme.ui.Theme
import java.util.Date

@Composable
fun ChatReadReceipt(
    readCount: Int,
    showTime: Boolean,
    time: Date,
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

        if (showTime) {
            Text(
                text = time.formattedTime(),
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
            ChatReadReceipt(
                readCount = 1,
                showTime = true,
                time = Date(),
                alignment = Alignment.End
            )

            ChatReadReceipt(
                readCount = 0,
                showTime = true,
                time = Date(),
                alignment = Alignment.Start,
            )
        }
    }
}