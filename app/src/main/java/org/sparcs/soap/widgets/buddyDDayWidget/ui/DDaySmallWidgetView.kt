package org.sparcs.soap.widgets.buddyDDayWidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
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
import org.sparcs.soap.R
import org.sparcs.soap.widgets.buddyDDayWidget.DDayType
import org.sparcs.soap.widgets.buddyDDayWidget.DDayWidgetEntry

@Composable
fun DDaySmallWidgetView(entry: DDayWidgetEntry) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Text(
            text = if (entry.type == DDayType.SEMESTER_ENDED) {
                context.getString(R.string.d_day_widget_ended).uppercase()
            } else {
                subtitleText(entry).uppercase()
            },
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        if (entry.type == DDayType.SEMESTER_ENDED) {
            Text(
                text = context.getString(R.string.d_day_widget_semester_ended),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 2
            )
        } else {
            Row(verticalAlignment = Alignment.Vertical.Bottom) {
                Text(
                    text = "${entry.days}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = context.getString(R.string.days_suffix),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = GlanceModifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = entry.semesterLabel,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
@Suppress("unused")
private fun DDaySmallWidgetPreview() {
    GlanceTheme { DDaySmallWidgetView(DDayWidgetEntry.mock()) }
}
