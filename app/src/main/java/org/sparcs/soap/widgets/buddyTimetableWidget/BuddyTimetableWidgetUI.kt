package org.sparcs.soap.widgets.buddyTimetableWidget

import android.content.Context
import android.graphics.Paint
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.helpers.TimetableConstructor.hoursWidth
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.app.features.timetable.components.PACKED_TEXT_SCALE
import org.sparcs.soap.app.features.timetable.components.TimetableCellLines
import org.sparcs.soap.app.features.timetable.components.TimetableCellMetrics
import org.sparcs.soap.app.shared.formatters.formatHour
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry
import org.sparcs.soap.widgets.theme.ui.TimetableWidgetTheme.grayBB
import kotlin.math.roundToInt

@Composable
private fun TimetableWidgetCell(
    lecture: WidgetLectureEntry,
    width: Dp,
    height: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val title = lecture.title ?: return
    val classroom = lecture.classroom ?: return
    if (lecture.startMinutes == null || lecture.durationMinutes == null) return

    val context = LocalContext.current
    val cellHeight = (height - CELL_GAP).coerceAtLeast(0.dp)
    val metrics = TimetableCellMetrics.of(width, cellHeight)
    val titleSize = if (metrics.isPacked) TITLE_FONT_SIZE * PACKED_TEXT_SCALE else TITLE_FONT_SIZE
    val room = lecture.packedClassroom?.takeIf { metrics.isPacked } ?: classroom
    val subtitle = lecture.subtitle?.takeIf { it.isNotBlank() }
    val lines = context.widgetCellLines(metrics, width, cellHeight, titleSize, subtitle, room)

    val backgroundColor = Color(lecture.bgColor.toColorInt())
    val textColor = Color(lecture.textColor.toColorInt())
    val detailColor = textColor.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(bottom = CELL_GAP)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding)
                .cornerRadius(4.dp)
                .background(backgroundColor.solid()),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = TextStyle(color = textColor.solid(), fontSize = titleSize, fontWeight = FontWeight.Medium),
                maxLines = lines.title
            )

            if (lines.room > 0) {
                if (subtitle != null && lines.subtitle > 0) {
                    Spacer(modifier = GlanceModifier.height(lines.spacing))
                    Text(
                        text = subtitle,
                        style = TextStyle(color = textColor.solid(), fontSize = DETAIL_FONT_SIZE),
                        maxLines = lines.subtitle
                    )
                }

                Spacer(modifier = GlanceModifier.height(lines.spacing))
                Text(
                    text = room,
                    style = TextStyle(color = detailColor.solid(), fontSize = DETAIL_FONT_SIZE),
                    maxLines = lines.room
                )
            }
        }
    }
}

private fun Context.widgetCellLines(
    metrics: TimetableCellMetrics,
    width: Dp,
    height: Dp,
    titleSize: TextUnit,
    subtitle: String?,
    room: String,
): TimetableCellLines {
    val density = resources.displayMetrics.density
    val textWidth = ((width - metrics.horizontalPadding * 2).value * density).toInt()
    val titlePaint = textPaint(titleSize)
    val detailPaint = textPaint(DETAIL_FONT_SIZE)

    return TimetableCellLines.of(
        height = height - metrics.verticalPadding * 2,
        titleLineHeight = (titlePaint.lineHeight() / density).dp,
        detailLineHeight = (detailPaint.lineHeight() / density).dp,
        spacing = DETAIL_SPACING,
        maxTitleLines = metrics.titleMaxLines,
        maxSubtitleLines = subtitle?.let { minOf(metrics.subtitleMaxLines, detailPaint.lineCount(it, textWidth)) } ?: 0,
        maxRoomLines = if (room.isBlank()) 0 else minOf(metrics.roomMaxLines, detailPaint.lineCount(room, textWidth)),
    )
}

private fun Context.textPaint(fontSize: TextUnit): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize.value, resources.displayMetrics)
}

private fun TextPaint.lineHeight(): Int = layout(LINE_SAMPLE, Int.MAX_VALUE).height

private fun TextPaint.lineCount(text: String, width: Int): Int = layout(text, width).lineCount

private fun TextPaint.layout(text: String, width: Int): StaticLayout =
    StaticLayout.Builder.obtain(text, 0, text.length, this, width.coerceAtLeast(1)).build()

private fun Color.solid(): ColorProvider = ColorProvider(day = this, night = this)

@Composable
fun TimetableLargeWidgetView(
    timetable: WidgetTimetableEntry?,
    theme: TimetableTheme = TimetableTheme.Default,
) {
    val size = LocalSize.current
    val labelColor = theme.gridLabelColor?.solid()
    val lineColor = theme.separatorColor?.solid() ?: GlanceTheme.colors.grayBB
    // A theme separator is an explicit choice, so it is drawn as picked; the default grid keeps its faint hour lines.
    val solidLineAlpha = if (theme.separatorColor != null) 1f else 0.15f
    val minMin = timetable?.minMinutes ?: 540 // 9:00 AM
    val maxMin = timetable?.maxMinutes ?: 1080 // 6:00 PM
    val visibleDays = timetable?.visibleDays ?: emptyList()

    val startHour = minMin / 60
    val endHour = (maxMin + 59) / 60
    val totalHours = (endHour - startHour).coerceAtLeast(1)

    val context = LocalContext.current
    val metrics = context.resources.displayMetrics
    val fontScale = context.resources.configuration.fontScale
    val headerHeight = 22.dp
    val labelHeight = 16 * fontScale
    val topInset = labelHeight / 2
    val availableHeight = (size.height.value - headerHeight.value - topInset).coerceAtLeast(0f)
    val cellWidth = ((size.width - WIDGET_PADDING * 2 - hoursWidth - 8.dp) / visibleDays.size.coerceAtLeast(1) - 4.dp).coerceAtLeast(0.dp)

    // Resolve every boundary on the same pixel scale. Stacking rounded hour rows
    // accumulates rounding errors, especially across a full-day timetable.
    fun position(minutes: Int): Float =
        (((minutes - startHour * 60) * availableHeight / (totalHours * 60)) * metrics.density)
            .roundToInt() / metrics.density

    Column(modifier = GlanceModifier.fillMaxSize()) {
        DaysColumnHeader(visibleDays, labelColor ?: GlanceTheme.colors.onSurface)

        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            Box(modifier = GlanceModifier.width(hoursWidth + 8.dp).fillMaxHeight()) {
                // Glance containers support at most ten direct children.
                (startHour until endHour).toList().chunked(10).forEach { hours ->
                    Box(modifier = GlanceModifier.fillMaxSize()) {
                        hours.forEach { hour ->
                            Box(modifier = GlanceModifier.padding(top = position(hour * 60).dp)) {
                                Box(
                                    modifier = GlanceModifier.fillMaxWidth().height(labelHeight.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(context.formatHour(hour), style = TextStyle(fontSize = 10.sp, color = labelColor ?: GlanceTheme.colors.onSurfaceVariant, textAlign = TextAlign.Center))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = GlanceModifier.fillMaxSize()
                    .padding(start = hoursWidth + 8.dp, top = topInset.dp)
            ) {
                visibleDays.forEach { day ->
                    Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(horizontal = 2.dp)) {
                        (startHour * 60 until endHour * 60 step 30).toList().chunked(10).forEach { times ->
                            Box(modifier = GlanceModifier.fillMaxSize()) {
                                times.forEach { minutes ->
                                    Box(modifier = GlanceModifier.fillMaxWidth().padding(top = position(minutes).dp)) {
                                        if (minutes % 60 == 0) HorizontalLine(color = lineColor, alpha = solidLineAlpha)
                                        else DashedHorizontalLine(color = lineColor)
                                    }
                                }
                            }
                        }
                        timetable?.getEntries(day).orEmpty().chunked(10).forEach { entries ->
                            Box(modifier = GlanceModifier.fillMaxSize()) {
                                entries.forEach { entry ->
                                    val begin = entry.startMinutes ?: startHour * 60
                                    val top = position(begin)
                                    val bottom = position(begin + (entry.durationMinutes ?: 0))
                                    Box(modifier = GlanceModifier.fillMaxWidth().padding(top = top.dp)) {
                                        TimetableWidgetCell(lecture = entry, width = cellWidth, height = (bottom - top).dp)
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
private fun DaysColumnHeader(visibleDays: List<DayType>, color: ColorProvider) {
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
                    color = color
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

private val WIDGET_PADDING = 8.dp
private val CELL_GAP = 4.dp
private val DETAIL_SPACING = 2.dp
private val TITLE_FONT_SIZE = 10.sp
private val DETAIL_FONT_SIZE = 8.sp
private const val LINE_SAMPLE = "Ag가"

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

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 400)
@Composable
@Suppress("unused")
private fun TimetablePackedGridPreview() {
    Column(modifier = GlanceModifier.fillMaxSize().background(Color.White.solid())) {
        TimetableLargeWidgetView(
            timetable = WidgetTimetableEntry.mock()
        )
    }
}
