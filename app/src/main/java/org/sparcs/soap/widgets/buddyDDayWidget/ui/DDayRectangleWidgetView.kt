package org.sparcs.soap.widgets.buddyDDayWidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.soap.widgets.buddyDDayWidget.DDayType
import org.sparcs.soap.widgets.buddyDDayWidget.DDayWidgetEntry

@Composable
fun DDayRectangleWidgetView(entry: DDayWidgetEntry) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "•",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = entry.semesterLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1
            )
            if (entry.type != DDayType.SEMESTER_ENDED) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = formatDDay(entry.days),
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = subtitleText(entry),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(8.dp))
        DDayProgressBar(progress = entry.progress)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 100)
@Composable
@Suppress("unused")
private fun DDayRectangleWidgetPreview() {
    GlanceTheme { DDayRectangleWidgetView(DDayWidgetEntry.mock()) }
}
