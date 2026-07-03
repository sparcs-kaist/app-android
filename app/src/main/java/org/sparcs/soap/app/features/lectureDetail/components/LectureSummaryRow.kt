package org.sparcs.soap.app.features.lectureDetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.sparcs.soap.app.theme.ui.Theme

@Composable
fun LectureSummaryRow(
    title: String,
    description: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = description,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme { LectureSummaryRow(title = "Language", description = "EN") }
}
