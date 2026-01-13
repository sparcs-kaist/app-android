package org.sparcs.Widgets.BuddyUpcomingClassWidget.UI

import android.graphics.Color.parseColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.sparcs.R
import org.sparcs.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.Widgets.BuddyUpcomingClassWidget.mock
import java.time.LocalDate
import java.util.Locale

@Composable
fun UpcomingClassSmallWidgetView(entry: WidgetLectureEntry) {
    val context = LocalContext.current
    val backgroundColor = Color(parseColor(entry.bgColor))

    if (entry.signInRequired) {
        Box(
            modifier = GlanceModifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = context.getString(R.string.login_required_long),
                style = TextStyle(textAlign = TextAlign.Center, fontSize = 13.sp, color = GlanceTheme.colors.onSurface)
            )
        }
    } else if (entry.title != null && entry.classroom != null) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = context.getString(R.string.up_next),
                style = TextStyle(
                    color = ColorProvider(backgroundColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = entry.title,
                maxLines = 2,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = entry.formattedTimeRange,
                style = TextStyle(
                    color = ColorProvider(backgroundColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = entry.classroom,
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    } else {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            val today = LocalDate.now()

            Text(
                text = today.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    Locale.getDefault()
                ).uppercase(),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = today.dayOfMonth.toString(),
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Text(
                text = context.getString(R.string.no_more_classes),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Text(
                text = context.getString(R.string.enjoy_day),
                maxLines = 1,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassSmallWidgetPreview() {
    GlanceTheme { UpcomingClassSmallWidgetView(WidgetLectureEntry.mock()) }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassSmallWidgetPreviewLoginRequired() {
    GlanceTheme {
        UpcomingClassSmallWidgetView(
            WidgetLectureEntry.mock().copy(signInRequired = true)
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassSmallWidgetPreviewNoClass() {
    GlanceTheme { UpcomingClassSmallWidgetView(WidgetLectureEntry.mock().copy(title = null)) }
}
