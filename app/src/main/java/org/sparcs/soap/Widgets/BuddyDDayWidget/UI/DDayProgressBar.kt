package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height

@Composable
fun DDayProgressBar(progress: Float) {
    val progressValue = (progress.coerceIn(0f, 1f) * 100).toInt()
    val remainingValue = 100 - progressValue
    val inactiveColor = ColorProvider(day = Color(0xFFE5E7EB), night = Color(0xFF2A2F3A))
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(6.dp)
            .cornerRadius(999.dp)
            .background(inactiveColor)
    ) {
        if (progressValue > 0) {
            Row(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(6.dp)
                    .background(GlanceTheme.colors.primary)
            ) {
                repeat(progressValue) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }

        if (remainingValue > 0) {
            Row(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(6.dp)
            ) {
                repeat(remainingValue) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }
}