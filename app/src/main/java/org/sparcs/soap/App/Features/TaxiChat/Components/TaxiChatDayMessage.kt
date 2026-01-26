package org.sparcs.soap.App.Features.TaxiChat.Components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaxiChatDayMessage(date: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DashedDivider(modifier = Modifier.weight(1f))
        Text(
            text = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        DashedDivider(modifier = Modifier.weight(1f))
    }

}

@Composable
fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    Canvas(modifier = modifier.height(1.dp)) {
        val dashWidth = 10f
        val gapWidth = 12f
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x + dashWidth, 0f),
            )
            x += dashWidth + gapWidth
        }
    }
}


@Preview
@Composable
private fun Preview() {
    TaxiChatDayMessage(date = LocalDate.now())
}
