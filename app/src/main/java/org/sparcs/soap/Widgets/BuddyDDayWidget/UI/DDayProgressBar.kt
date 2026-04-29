package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width

@Composable
fun DDayProgressBar(progress: Float) {
    val totalSegments = 16
    val activeSegments = (progress.coerceIn(0f, 1f) * totalSegments).toInt().coerceIn(0, totalSegments)
    val inactiveColor = ColorProvider(day = Color(0xFFE5E7EB), night = Color(0xFF2A2F3A))

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        repeat(totalSegments) { index ->
            Box(
                modifier = GlanceModifier
                    .width(0.dp)
                    .defaultWeight()
                    .height(6.dp)
                    .padding(horizontal = 1.dp)
                    .cornerRadius(999.dp)
                    .background(if (index < activeSegments) GlanceTheme.colors.primary else inactiveColor)
            ) {}
        }
    }
}



