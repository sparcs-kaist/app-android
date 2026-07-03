package org.sparcs.soap.widgets.buddyDDayWidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height

@Composable
fun DDayProgressBar(progress: Float) {
    val inactiveColor = ColorProvider(day = Color(0xFFE5E7EB), night = Color(0xFF2A2F3A))
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(6.dp)
            .cornerRadius(999.dp)
    ) {
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = GlanceModifier.fillMaxSize(),
            color = GlanceTheme.colors.primary,
            backgroundColor = inactiveColor
        )
    }
}
