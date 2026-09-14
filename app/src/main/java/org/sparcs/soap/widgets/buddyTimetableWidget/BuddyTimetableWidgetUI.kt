package org.sparcs.soap.widgets.buddyTimetableWidget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlin.math.ceil
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableConstructor.hoursWidth
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.soap.widgets.theme.ui.TimetableWidgetTheme.grayBB

@Composable
private fun TimetableWidgetCell(
    lecture: WidgetLectureEntry,
    height: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    if (lecture.title == null || lecture.classroom == null || lecture.startMinutes == null || lecture.durationMinutes == null) return

    val compact = height < 44
    val inset = if (compact) 2 else 6
    val gap = if (compact) 2 else 4
    val fontSize = if (compact) 9 else 10
    val fontScale = LocalContext.current.resources.configuration.fontScale
    val titleLineHeight = ceil(fontSize * 1.3f * fontScale).toInt().coerceAtLeast(1)
    val locationLineHeight = ceil(8 * 1.3f * fontScale).toInt()
    val availableTextHeight = height - gap - inset * 2
    val showLocation = lecture.classroom.isNotBlank() && availableTextHeight >= titleLineHeight + locationLineHeight + 2
    val titleLines = ((availableTextHeight - if (showLocation) locationLineHeight + 2 else 0) / titleLineHeight).coerceIn(1, 3)
    val backgroundColor = Color(lecture.bgColor.toColorInt())
    val textColor = Color(lecture.textColor.toColorInt())
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = gap.dp)
            .height(height.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(inset.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(day = backgroundColor, night = backgroundColor)),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (availableTextHeight >= titleLineHeight) Text(
                text = lecture.title,
                style = TextStyle(
                    color = ColorProvider(day = textColor, night = textColor),
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = titleLines
            )

            if (showLocation) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = lecture.classroom,
                    style = TextStyle(
                        color = ColorProvider(day = textColor.copy(alpha = 0.8f), night = textColor.copy(alpha = 0.8f)),
                        fontSize = 8.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TimetableLargeWidgetView(timetable: WidgetTimetableEntry?) {
    val size = LocalSize.current
    val minMin = timetable?.minMinutes ?: 540 // 8:00 AM
    val maxMin = timetable?.maxMinutes ?: 1080 // 6:00 PM
    val visibleDays = timetable?.visibleDays ?: emptyList()

    val startHour = minMin / 60
    val endHour = (maxMin + 59) / 60
    val totalHours = (endHour - startHour).coerceAtLeast(1)

    val headerHeight = 22.dp
    val availableHeight = (size.height - headerHeight - 6.dp).coerceAtLeast(0.dp)
    val minuteHeight = availableHeight.value / (totalHours * 60f)
    val dynamicHourHeight = minuteHeight * 60f

    Column(modifier = GlanceModifier.fillMaxSize()) {
        DaysColumnHeader(visibleDays)

        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Glance containers support at most ten direct children.
                (startHour until endHour).toList().chunked(8).forEach { hours ->
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        hours.forEach { hour ->
                            Box(modifier = GlanceModifier.fillMaxWidth().height(dynamicHourHeight.dp)) {
                                Row(modifier = GlanceModifier.fillMaxSize().padding(start = hoursWidth + 8.dp, top = 6.dp)) {
                                    visibleDays.forEach { _ ->
                                        Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 2.dp)) {
                                            HorizontalLine(alpha = 0.15f)
                                            Spacer(modifier = GlanceModifier.defaultWeight())
                                            DashedHorizontalLine()
                                            Spacer(modifier = GlanceModifier.defaultWeight())
                                        }
                                    }
                                }
                                Box(modifier = GlanceModifier.width(hoursWidth + 8.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                                    Text(hour.toString(), style = TextStyle(fontSize = 10.sp, color = GlanceTheme.colors.onSurfaceVariant, textAlign = TextAlign.Center))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = GlanceModifier.fillMaxSize()
                    .padding(start = hoursWidth + 8.dp, top = 6.dp)
            ) {
                visibleDays.forEach { day ->
                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                        timetable?.getEntries(day).orEmpty().chunked(10).forEach { entries ->
                            Box(modifier = GlanceModifier.fillMaxSize()) {
                                entries.forEach { entry ->
                                    val topOffset = ((entry.startMinutes ?: startHour * 60) - startHour * 60) * minuteHeight
                                    val cellHeight = (entry.durationMinutes ?: 0) * minuteHeight
                                    Box(modifier = GlanceModifier.padding(top = topOffset.dp).padding(horizontal = 2.dp)) {
                                        TimetableWidgetCell(lecture = entry, height = cellHeight.toInt())
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

@Composable
private fun DaysColumnHeader(visibleDays: List<DayType>) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier
            .padding(start = hoursWidth + 8.dp)
            .height(22.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleDays.forEach { day ->
            Text(
                text = context.getString(day.stringValue),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun HorizontalLine(
    modifier: GlanceModifier = GlanceModifier,
    color: ColorProvider = GlanceTheme.colors.grayBB,
    alpha: Float = 1f,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(1.dp)
            .height(1.dp)
            .background(ColorProvider(day = color.getColor(context).copy(alpha), night = color.getColor(context).copy(alpha)))
    ) {}
}

@Composable
private fun DashedHorizontalLine(
    color: ColorProvider = GlanceTheme.colors.grayBB,
) {
    Image(
        provider = ImageProvider(R.drawable.dash_line),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = GlanceModifier.height(2.dp)
            .fillMaxWidth(),
        colorFilter = ColorFilter.tint(color)
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 300, heightDp = 600)
@Composable
@Suppress("unused")
private fun TimetableGridPreview() {
    Column(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(day = Color.White, night = Color.White))) {
        TimetableLargeWidgetView(
            timetable = WidgetTimetableEntry.mock()
        )
    }
}