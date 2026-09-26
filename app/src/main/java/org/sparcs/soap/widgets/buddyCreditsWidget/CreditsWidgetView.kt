package org.sparcs.soap.widgets.buddyCreditsWidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.shared.formatters.creditProgress
import org.sparcs.soap.app.shared.formatters.formatGPA
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import org.sparcs.soap.widgets.timetableWidgetIntent

@Composable
internal fun CreditsWidgetView(snapshot: CreditSummarySnapshot?, signInRequired: Boolean = false) {
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < 110.dp
    val wide = size.width >= 240.dp
    val complete = snapshot != null && snapshot.earnedCredits >= snapshot.graduationCredits
    val progressColor = if (complete) ColorProvider(Color(0xFF34A853)) else GlanceTheme.colors.primary
    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background)
            .cornerRadius(20.dp).padding(if (compact) 8.dp else 16.dp)
            .clickable(actionStartActivity(timetableWidgetIntent(context))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!compact) {
            Text(context.getString(R.string.credit_calculation), style = TextStyle(
                color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Spacer(GlanceModifier.defaultWeight())
        }
        if (snapshot == null) {
            Text(context.getString(if (signInRequired) R.string.login_required else R.string.credit_widget_open),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = if (compact) 12.sp else 14.sp),
                maxLines = if (compact) 2 else 4)
        } else {
            if (compact) {
                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${context.getString(R.string.credit_gpa_label)} ${formatGPA(snapshot.gpa)}",
                        modifier = GlanceModifier.defaultWeight(), maxLines = 1,
                        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = if (wide) 18.sp else 13.sp, fontWeight = FontWeight.Bold))
                    Text("${if (complete) "✓ " else ""}${snapshot.earnedCredits}/${snapshot.graduationCredits}", maxLines = 1,
                        style = TextStyle(color = if (complete) progressColor else GlanceTheme.colors.onSurfaceVariant,
                            fontSize = if (wide) 16.sp else 12.sp, fontWeight = FontWeight.Bold))
                }
            } else if (wide) {
                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(GlanceModifier.defaultWeight()) {
                        WidgetSummaryValue(context.getString(R.string.credit_gpa_label), formatGPA(snapshot.gpa), "4.3")
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        WidgetSummaryValue(context.getString(R.string.credit_units_label), "${snapshot.earnedCredits}", "${snapshot.graduationCredits}")
                    }
                }
            } else {
                WidgetSummaryValue(context.getString(R.string.credit_gpa_label), formatGPA(snapshot.gpa), "4.3")
                Spacer(GlanceModifier.height(8.dp))
                Text(context.getString(R.string.credit_widget_progress, snapshot.earnedCredits, snapshot.graduationCredits),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium))
            }
            Spacer(GlanceModifier.height(if (compact) 4.dp else 12.dp))
            LinearProgressIndicator(
                progress = creditProgress(snapshot.earnedCredits, snapshot.graduationCredits),
                modifier = GlanceModifier.fillMaxWidth().height(if (compact) 4.dp else 10.dp).cornerRadius(5.dp),
                color = progressColor, backgroundColor = GlanceTheme.colors.surfaceVariant,
            )
        }
        if (!compact) Spacer(GlanceModifier.defaultWeight())
    }
}

@Composable
private fun WidgetSummaryValue(title: String, value: String, total: String) {
    Text(title, style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value, style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold))
        Text(" / $total", style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp))
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Preview(widthDp = 320, heightDp = 150)
@Preview(widthDp = 320, heightDp = 60)
@Preview(widthDp = 170, heightDp = 60)
@Composable
private fun CreditsWidgetPreview() {
    WidgetTheme { CreditsWidgetView(CreditSummarySnapshot(3.73, 96, 138)) }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 60)
@Composable
private fun EmptyCreditsWidgetPreview() { WidgetTheme { CreditsWidgetView(null) } }
