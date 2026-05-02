package org.sparcs.soap.Widgets.AraPortalWidget.UI

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.AraPortalWidget.AraPortalUiState
import org.sparcs.soap.Widgets.theme.ui.TimetableWidgetTheme

@Composable
fun AraPortalWidgetContent(
    state: AraPortalUiState,
    visibleCount: Int,
) {
    val context = LocalContext.current

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = context.getString(R.string.ara_portal_widget_header),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
        }

        when {
            state.signInRequired -> {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                EmptyMessage(
                    message = context.getString(R.string.ara_portal_widget_sign_in_required),
                    modifier = if (launchIntent != null) {
                        GlanceModifier.clickable(actionStartActivity(launchIntent))
                    } else {
                        GlanceModifier
                    }
                )
            }
            state.isLoading -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.notices.isEmpty() -> EmptyMessage(context.getString(R.string.ara_portal_widget_empty))
            else -> state.notices.take(visibleCount).forEach { entry ->
                NoticeRow(
                    entry = entry,
                    onClick = actionStartActivity(openPostIntent(entry.id))
                )
            }
        }
    }
}

@Composable
private fun EmptyMessage(
    message: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Spacer(modifier = GlanceModifier.height(1.dp))
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                fontSize = 13.sp,
                color = TimetableWidgetTheme.grayBB
            )
        )
    }
}

private fun openPostIntent(postId: Int): Intent {
    return Intent(Intent.ACTION_VIEW, "${Constants.araShareURL}$postId".toUri())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
