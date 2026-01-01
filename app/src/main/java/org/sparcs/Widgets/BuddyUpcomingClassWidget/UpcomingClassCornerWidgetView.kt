package org.sparcs.Widgets.BuddyUpcomingClassWidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.R

@Composable
fun UpcomingClassCornerWidgetView(entry: WidgetLectureEntry) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier.fillMaxSize().padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (entry.signInRequired) {
            CornerStatusView(context.getString(R.string.login_required))

        } else if (entry.title != null && entry.classroom != null) {
            val startTime = entry.formattedTimeRange.split("-")[0].trim()

            Text(
                text = startTime,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )

            Text(
                text = entry.title,
                maxLines = 1,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            CornerStatusView(context.getString(R.string.no_more_classes))
        }
    }
}

@Composable
private fun CornerStatusView(label: String) {
    Text(
        text = label,
        maxLines = 2,
        style = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = GlanceTheme.colors.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 60, heightDp = 60)
@Composable
private fun UpcomingClassCornerPreview() {
    UpcomingClassCornerWidgetView(WidgetLectureEntry.mock())
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 60, heightDp = 60)
@Composable
private fun UpcomingClassCornerPreviewLoginRequired() {
    UpcomingClassCornerWidgetView(WidgetLectureEntry.mock().copy(signInRequired = true))
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 60, heightDp = 60)
@Composable
private fun UpcomingClassCornerPreviewNoClass() {
    UpcomingClassCornerWidgetView(WidgetLectureEntry.mock().copy(title = null))
}