package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import org.sparcs.soap.R

@Composable
internal fun DDayStatusColumn(title: String, subtitle: String) {
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