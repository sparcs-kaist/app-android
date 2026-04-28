package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayType
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetEntry

@Composable
fun DDayCircularWidgetView(entry: DDayWidgetEntry) {
    if (isFinished(entry)) {
        DDayFinishedView()
        return
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(999.dp)
                .background(GlanceTheme.colors.primary)
                .padding(horizontal = 12.dp, vertical = 10.dp)
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatDDay(entry.days),
                style = TextStyle(
                    fontSize = 13.sp,
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

@Composable
fun DDayRectangleWidgetView(entry: DDayWidgetEntry) {
    if (isFinished(entry)) {
        DDayFinishedView()
        return
    }

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
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
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = entry.semesterLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = formatDDay(entry.days),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = subtitleText(entry),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1
        )

        Spacer(modifier = GlanceModifier.height(8.dp))
        DDayProgressBar(progress = entry.progress)
    }
}

@Composable
fun DDaySmallWidgetView(entry: DDayWidgetEntry) {
    if (isFinished(entry)) {
        DDayFinishedView()
        return
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
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
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = entry.semesterLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = formatDDay(entry.days),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        Text(
            text = subtitleText(entry),
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 2
        )

        Spacer(modifier = GlanceModifier.defaultWeight())
        DDayProgressBar(progress = entry.progress)
    }
}

@Composable
fun DDayLoadingView() {
    val context = LocalContext.current
    DDayStatusColumn(
        title = context.getString(R.string.d_day_widget_loading),
        subtitle = context.getString(R.string.d_day_widget_wait_moment)
    )
}

@Composable
fun DDaySignInRequiredView() {
    val context = LocalContext.current
    DDayStatusColumn(
        title = context.getString(R.string.d_day_widget_sign_in_required),
        subtitle = context.getString(R.string.d_day_widget_sync_required)
    )
}

@Composable
fun DDayErrorView() {
    val context = LocalContext.current
    DDayStatusColumn(
        title = context.getString(R.string.d_day_widget_error),
        subtitle = context.getString(R.string.d_day_widget_loading_failed)
    )
}

@Composable
fun DDayFinishedView() {
    val context = LocalContext.current
    DDayStatusColumn(
        title = context.getString(R.string.no_more_classes),
        subtitle = context.getString(R.string.enjoy_day)
    )
}

@Composable
private fun DDayStatusColumn(title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(10.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            maxLines = 2
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                color = GlanceTheme.colors.onSurface,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            ),
            maxLines = 3
        )
    }
}

@Composable
private fun DDayProgressBar(progress: Float) {
    val activeSegments = (progress.coerceIn(0f, 1f) * 20f).toInt().coerceIn(0, 20)
    val inactiveColor = ColorProvider(day = Color(0xFF2A2F3AL), night = Color(0xFF2A2F3AL))

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        repeat(20) { index ->
            Box(
                modifier = GlanceModifier
                    .width(0.dp)
                    .defaultWeight()
                    .height(6.dp)
                    .padding(horizontal = 1.dp)
                    .cornerRadius(999.dp)
                    .background(if (index < activeSegments) GlanceTheme.colors.primary else inactiveColor)
            ) {}
        }
    }
}

@Composable
private fun subtitleText(entry: DDayWidgetEntry): String {
    val context = LocalContext.current
    return when (entry.type) {
        DDayType.START_OF_SEMESTER -> context.getString(R.string.d_day_widget_starts_in_days, kotlin.math.abs(entry.days))
        DDayType.END_OF_SEMESTER -> context.getString(R.string.d_day_widget_ends_in_days, kotlin.math.abs(entry.days))
        DDayType.ERROR -> context.getString(R.string.d_day_widget_error)
    }
}

@Composable
private fun formatDDay(days: Int): String {
    val context = LocalContext.current
    return when {
        days > 0 -> context.getString(R.string.d_day_widget_d_minus, days)
        days < 0 -> context.getString(R.string.d_day_widget_d_plus, kotlin.math.abs(days))
        else -> context.getString(R.string.d_day_widget_d_day)
    }
}

private fun isFinished(entry: DDayWidgetEntry): Boolean {
    return entry.type == DDayType.END_OF_SEMESTER && entry.days <= 0
}
