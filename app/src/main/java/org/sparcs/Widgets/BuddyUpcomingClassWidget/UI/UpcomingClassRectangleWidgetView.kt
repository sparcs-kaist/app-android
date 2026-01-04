package org.sparcs.Widgets.BuddyUpcomingClassWidget.UI

import android.graphics.Color.parseColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.sparcs.R
import org.sparcs.Widgets.BuddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.Widgets.BuddyUpcomingClassWidget.mock

@Composable
fun UpcomingClassRectangleWidgetView(entry: WidgetLectureEntry) {
    val context = LocalContext.current
    val backgroundColor = Color(parseColor(entry.bgColor))

    if (entry.signInRequired) {
        RectangleStatusView(
            title = context.getString(R.string.login_required),
            description = context.getString(R.string.open_app_prompt)
        )
    } else if (entry.title != null && entry.classroom != null) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .size(8.dp)
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(5.dp)
                ) {}

                Spacer(modifier = GlanceModifier.width(6.dp))

                Text(
                    text = entry.formattedTimeRange,
                    style = TextStyle(
                        color = ColorProvider(backgroundColor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = entry.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )

            Text(
                text = entry.classroom,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )

        }
    } else {
        RectangleStatusView(
            title = context.getString(R.string.no_more_classes),
            description = context.getString(R.string.enjoy_day)
        )
    }
}

@Composable
private fun RectangleStatusView(title: String, description: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(GlanceTheme.colors.primary)
                    .cornerRadius(5.dp)
            ) {}
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                ),
                maxLines = 1
            )
        }
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = description,
            style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassRectanglePreview() {
    GlanceTheme {
        UpcomingClassRectangleWidgetView(
            entry = WidgetLectureEntry.mock()
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassRectanglePreviewLoginRequired() {
    GlanceTheme {
        UpcomingClassRectangleWidgetView(
            entry = WidgetLectureEntry.mock().copy(signInRequired = true)
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun UpcomingClassRectanglePreviewNoClass() {
    GlanceTheme {
        UpcomingClassRectangleWidgetView(
            entry = WidgetLectureEntry.mock().copy(title = null)
        )
    }
}