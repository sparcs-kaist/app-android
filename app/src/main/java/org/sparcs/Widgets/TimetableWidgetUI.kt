package org.sparcs.Widgets

import android.graphics.Color.parseColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.background
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
import org.sparcs.App.Domain.Enums.OTL.DayType
import org.sparcs.App.Domain.Helpers.TimetableConstructor.hoursWidth
import org.sparcs.R
import org.sparcs.Widgets.theme.ui.TimetableWidgetTheme.grayBB

@Composable
private fun TimetableGridCell(
    lecture: WidgetLectureEntry,
    height: Int,
    modifier: GlanceModifier = GlanceModifier
) {
    val backgroundColor = Color(parseColor(lecture.bgColor))
    val textColor = Color(parseColor(lecture.textColor))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .height(height.dp)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(6.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(backgroundColor)),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = lecture.title,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 3
            )

            if (height > 40) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = lecture.classroom,
                    style = TextStyle(
                        color = ColorProvider(textColor.copy(alpha = 0.8f)),
                        fontSize = 8.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
@Composable
fun TimetableLargeWidgetView(timetable: WidgetTimetable?) {
    val size = LocalSize.current
    val minMin = timetable?.minMinutes ?: 540
    val maxMin = timetable?.maxMinutes ?: 1260
    val visibleDays = timetable?.visibleDays ?: emptyList()

    val totalHoursCount = (maxMin - minMin) / 60f + 1
    val dynamicHourHeight = maxOf(40.dp, (size.height - 30.dp) / totalHoursCount)
    val minuteHeight = dynamicHourHeight.value / 60f
    val totalHeight = totalHoursCount * dynamicHourHeight.value

    Column(modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface)) {
            DaysColumnHeader(visibleDays)

            Box(modifier = GlanceModifier.fillMaxWidth().height(totalHeight.dp)) {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    for (hour in (minMin / 60)..(maxMin / 60)) {
                        item {
                            Row(modifier = GlanceModifier.fillMaxWidth().height(dynamicHourHeight)) {
                                Box(
                                    modifier = GlanceModifier.width(hoursWidth + 8.dp).fillMaxHeight(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(text = hour.toString(), style = TextStyle(fontSize = 12.sp))
                                }

                                Row(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                                    visibleDays.forEach { _ ->
                                        Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 2.dp).padding(top = 8.dp)) {
                                            Column(modifier = GlanceModifier.fillMaxSize()) {
                                                HorizontalLine(alpha = 0.2f)
                                                Spacer(modifier = GlanceModifier.defaultWeight())
                                                DashedHorizontalLine()
                                                Spacer(modifier = GlanceModifier.defaultWeight())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = GlanceModifier.fillMaxSize().padding(start = hoursWidth + 8.dp, top = 8.dp)) {
                    visibleDays.forEach { day ->
                        Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                            timetable?.getLectures(day)?.forEach { lecture ->
                                val topOffset = (lecture.startMinutes - minMin) * minuteHeight
                                val lHeight = lecture.durationMinutes * minuteHeight

                                Box(modifier = GlanceModifier.padding(top = topOffset.dp).padding(horizontal = 4.dp)) {
                                    TimetableGridCell(
                                        lecture = lecture,
                                        height = lHeight.toInt()
                                    )
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
            .padding(start = hoursWidth + 8.dp, bottom = 2.dp, top = 2.dp)
            .fillMaxWidth()
    ) {
        visibleDays.forEach { day ->
            Text(
                text = context.getString(day.stringValue),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
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
    alpha: Float = 1f
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(1.dp)
            .height(1.dp)
            .background(ColorProvider(color.getColor(context).copy(alpha)))
    ){}
}

@Composable
private fun DashedHorizontalLine(
    color: ColorProvider = GlanceTheme.colors.grayBB
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
private fun TimetableGridPreview() {
    Column(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(Color.White))) {
        TimetableLargeWidgetView(
            timetable = WidgetTimetable.mock()
        )
    }
}