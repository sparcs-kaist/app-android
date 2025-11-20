package com.sparcs.soap.Features.TaxiChat.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaxiChatDayMessage(date: LocalDate) {
    Text(
        text = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
        fontWeight = FontWeight.Medium,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun Preview() {
    TaxiChatDayMessage(date = LocalDate.now())
}
