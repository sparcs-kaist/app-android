package org.sparcs.soap.widgets.buddyCreditsWidget

import androidx.compose.runtime.Composable
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
import androidx.glance.layout.Box
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
    val wide = size.width >= 240.dp
    val title = context.getString(R.string.credit_calculation)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity(timetableWidgetIntent(context)))
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.defaultWeight())

            if (snapshot == null) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(if (signInRequired) R.string.login_required else R.string.credit_widget_open),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    )
                }
            } else {
                val gpaStr = formatGPA(snapshot.gpa)
                val earnedStr = "${snapshot.earnedCredits}"
                val gradStr = "${snapshot.graduationCredits}"

                if (wide) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = context.getString(R.string.credit_gpa_label),
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = gpaStr,
                                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = " / 4.3",
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = context.getString(R.string.credit_units_label),
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = earnedStr,
                                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = " / $gradStr",
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = context.getString(R.string.credit_gpa_label),
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = gpaStr,
                                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = " / 4.3",
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = context.getString(R.string.credit_units_label),
                                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 10.sp)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = earnedStr,
                                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = " / $gradStr",
                                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }

                Spacer(GlanceModifier.height(10.dp))

                LinearProgressIndicator(
                    progress = creditProgress(snapshot.earnedCredits, snapshot.graduationCredits),
                    modifier = GlanceModifier.fillMaxWidth().height(8.dp).cornerRadius(4.dp),
                    color = GlanceTheme.colors.primary,
                    backgroundColor = GlanceTheme.colors.surfaceVariant
                )
            }

            Spacer(GlanceModifier.defaultWeight())
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Preview(widthDp = 320, heightDp = 150)
@Composable
private fun CreditsWidgetPreview() {
    WidgetTheme { CreditsWidgetView(CreditSummarySnapshot(3.73, 96, 138)) }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 170, heightDp = 170)
@Composable
private fun EmptyCreditsWidgetPreview() {
    WidgetTheme { CreditsWidgetView(null) }
}
