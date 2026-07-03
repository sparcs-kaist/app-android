package org.sparcs.soap.widgets.buddyDDayWidget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
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
import org.sparcs.soap.R
import org.sparcs.soap.widgets.buddyDDayWidget.DDayType
import org.sparcs.soap.widgets.buddyDDayWidget.DDayWidgetEntry

@Composable
fun DDayCircularWidgetView(entry: DDayWidgetEntry) {
    val context = LocalContext.current
    val displayText = if (entry.type == DDayType.SEMESTER_ENDED) {
        context.getString(R.string.d_day_widget_ended)
    } else {
        formatDDay(entry.days)
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
                text = displayText,
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
@Suppress("unused")
private fun DDayCircularWidgetPreview() {
    GlanceTheme { DDayCircularWidgetView(DDayWidgetEntry.mock()) }
}
