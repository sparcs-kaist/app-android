package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetEntry

@Composable
fun DDaySmallWidgetView(entry: DDayWidgetEntry) {
    if (isFinished(entry)) {
        DDayFinishedView()
        return
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = subtitleText(entry).uppercase(),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Row(verticalAlignment = Alignment.Vertical.Bottom) {
            Text(
                text = "${entry.days}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Medium,
                )
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "days",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = GlanceModifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = entry.semesterLabel,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun DDaySmallWidgetPreview() {
    GlanceTheme { DDaySmallWidgetView(DDayWidgetEntry.mock()) }
}
