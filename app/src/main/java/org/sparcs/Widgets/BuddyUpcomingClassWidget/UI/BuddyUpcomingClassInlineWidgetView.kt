package org.sparcs.Widgets.BuddyUpcomingClassWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.R
import org.sparcs.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.Widgets.BuddyUpcomingClassWidget.mock

@Composable
fun UpcomingClassInlineWidgetView(entry: WidgetLectureEntry) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (entry.signInRequired) {
            Text(
                text = context.getString(R.string.login_required),
                maxLines = 1,
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurface)
            )
        } else if (entry.title != null) {
            val startTime = entry.formattedTimeRange.substringBefore("-").trim()
            Text(
                text = "$startTime • ${entry.title}",
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
        } else {
            Text(
                text = context.getString(R.string.no_more_classes),
                maxLines = 1,
                style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurface)
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 40)
@Composable
private fun UpcomingClassInlineWidgetPreview() {
    GlanceTheme { UpcomingClassInlineWidgetView(WidgetLectureEntry.mock()) }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 40)
@Composable
private fun UpcomingClassInlineWidgetPreviewLoginRequired() {
    GlanceTheme { UpcomingClassInlineWidgetView(WidgetLectureEntry.mock().copy(signInRequired = true)) }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 40)
@Composable
private fun UpcomingClassInlineWidgetPreviewNoClass() {
    GlanceTheme { UpcomingClassInlineWidgetView(WidgetLectureEntry.mock().copy(title = null)) }
}


