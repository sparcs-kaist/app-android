package org.sparcs.soap.widgets.buddyTimetableWidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.widgets.theme.ui.WidgetTheme
import java.time.LocalDate

@Composable
internal fun TimetableSmallWidgetView(timetable: WidgetTimetableEntry, theme: TimetableTheme) {
    val context = LocalContext.current
    val today = LocalDate.now().dayOfWeek.value - 1
    val labelColor = theme.gridLabelColor ?: GlanceTheme.colors.onSurface.getColor(context)
    val trackColor = labelColor.copy(alpha = 0.15f)
    val availableHeight = (LocalSize.current.height.value - 40f).coerceAtLeast(1f)
    val duration = (timetable.maxMinutes - timetable.minMinutes).coerceAtLeast(1)
    val blockGapDp = 2f
    Row(GlanceModifier.fillMaxSize()) {
        timetable.visibleDays.forEach { day ->
            Column(GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(context.getString(day.stringValue), modifier = GlanceModifier.fillMaxWidth().height(24.dp),
                    style = TextStyle(fontSize = 11.sp, textAlign = TextAlign.Center,
                        fontWeight = if (day.value == today) FontWeight.Bold else FontWeight.Normal,
                        color = ColorProvider(if (day.value == today) labelColor else labelColor.copy(alpha = 0.6f))))
                Box(GlanceModifier.fillMaxWidth().height(availableHeight.dp).cornerRadius(4.dp).background(trackColor)) {
                    timetable.getEntries(day).chunked(10).forEach { entries ->
                        Box(GlanceModifier.fillMaxSize()) {
                            entries.forEach entryLoop@ { entry ->
                                val start = entry.startMinutes ?: return@entryLoop
                                val length = entry.durationMinutes ?: return@entryLoop
                                val top = ((start - timetable.minMinutes).toFloat() / duration).coerceIn(0f, 1f)
                                val bottom = ((start + length - timetable.minMinutes).toFloat() / duration).coerceIn(top, 1f)
                                if (bottom > top) {
                                    val blockHeight = (((bottom - top) * availableHeight) - blockGapDp).coerceAtLeast(1f)
                                    Box(GlanceModifier.fillMaxWidth().padding(top = (top * availableHeight).dp)) {
                                        Box(GlanceModifier.fillMaxWidth().height(blockHeight.dp)
                                            .cornerRadius(3.dp).background(Color(entry.bgColor.toColorInt()))) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 150, heightDp = 160)
@Composable
private fun TimetableSmallWidgetPreview() {
    WidgetTheme { TimetableSmallWidgetView(WidgetTimetableEntry.mock(), TimetableTheme.Default) }
}
