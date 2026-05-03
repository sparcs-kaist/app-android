package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetEntry

@Composable
fun DDayCircularWidgetView(entry: DDayWidgetEntry) {
    if (isFinished(entry)) {
        DDayFinishedView()
        return
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(999.dp)
                .background(GlanceTheme.colors.primary)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatDDay(entry.days),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = entry.semesterLabel,
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 100, heightDp = 100)
@Composable
private fun DDayCircularWidgetPreview() {
    GlanceTheme { DDayCircularWidgetView(DDayWidgetEntry.mock()) }
}
