package org.sparcs.Widgets.BuddyUpcomingClassWidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.sparcs.R

@Composable
fun UpcomingClassCircularWidgetView(entry: WidgetLectureEntry) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (entry.signInRequired) {
            Image(
                provider = ImageProvider(R.drawable.round_login),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
            Text(
                text = context.getString(R.string.login_required),
                style = TextStyle(
                    fontSize = 11.sp, color = GlanceTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
            )
        } else if (entry.title != null) {
            Image(
                provider = ImageProvider(R.drawable.baseline_calendar_month),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )

            val startTime = entry.formattedTimeRange.split("-")[0].trim()
            Text(
                text = startTime,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.round_school),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp)
            )
            Text(
                text = context.getString(R.string.zero_left),
                style = TextStyle(fontSize = 11.sp, color = GlanceTheme.colors.onSurface)
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 70, heightDp = 70)
@Composable
private fun UpcomingClassCircularPreview() {
    GlanceTheme { UpcomingClassCircularWidgetView(WidgetLectureEntry.mock()) }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 70, heightDp = 70)
@Composable
private fun UpcomingClassCircularPreviewLoginRequired() {
    GlanceTheme {
        UpcomingClassCircularWidgetView(
            WidgetLectureEntry.mock().copy(signInRequired = true)
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 70, heightDp = 70)
@Composable
private fun UpcomingClassCircularPreviewNoClass() {
    GlanceTheme { UpcomingClassCircularWidgetView(WidgetLectureEntry.mock().copy(title = null)) }
}