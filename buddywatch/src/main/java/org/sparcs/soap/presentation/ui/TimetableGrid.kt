package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.ScheduleEntry
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.scheduleCells
import org.sparcs.soap.data.models.scheduleEntries
import org.sparcs.soap.data.models.visibleDays
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.shared.formatTimeRange
import org.sparcs.soap.shared.scheduleColor
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val GridSpacing = 3.dp
private val GridPadding = 14.dp

@Composable
fun WeekTimetableView(
    timetable: Timetable,
    today: DayOfWeek,
    now: LocalDateTime = LocalDateTime.now(),
    onShowOptions: (() -> Unit)? = null,
    onSelectDay: (DayOfWeek) -> Unit
) {
    TimetableGrid(timetable, timetable.visibleDays, today, now, onSelectDay, {}, onShowOptions)
}

@Composable
fun DayTimetableView(
    timetable: Timetable,
    day: DayOfWeek,
    now: LocalDateTime = LocalDateTime.now(),
    onShowOptions: (() -> Unit)? = null,
    onSelect: (ScheduleEntry) -> Unit
) {
    if (timetable.scheduleEntries(day).isEmpty()) {
        ScheduleScaffold(day.getDisplayName(TextStyle.FULL, Locale.getDefault()), onShowOptions = onShowOptions) {
            item { Text(stringResource(R.string.no_schedule), textAlign = TextAlign.Center) }
        }
    } else {
        TimetableGrid(timetable, listOf(day), now.dayOfWeek, now, {}, onSelect, onShowOptions)
    }
}

@Composable
private fun TimetableGrid(
    timetable: Timetable,
    days: List<DayOfWeek>,
    today: DayOfWeek,
    now: LocalDateTime,
    onSelectDay: (DayOfWeek) -> Unit,
    onSelect: (ScheduleEntry) -> Unit,
    onShowOptions: (() -> Unit)? = null
) {
    val entries = remember(timetable, days) { days.associateWith(timetable::scheduleEntries) }
    val allEntries = entries.values.flatten()
    val startMinutes = minOf(540, (allEntries.minOfOrNull { it.classTime.begin } ?: 540) / 60 * 60)
    val endMinutes = maxOf(1080, ((allEntries.maxOfOrNull { it.classTime.end } ?: 1080) + 59) / 60 * 60)
    val isWeek = days.size > 1
    val hourHeight = if (isWeek) 24.dp else 64.dp
    val gutterWidth = if (isWeek) 16.dp else 24.dp
    val firstMinutes = allEntries.minOfOrNull { it.classTime.begin } ?: startMinutes
    val initialMinutes = if (isWeek) startMinutes else (firstMinutes - 30).coerceAtLeast(startMinutes)
    val initialOffset = with(LocalDensity.current) {
        (hourHeight.toPx() * (initialMinutes - startMinutes) / 60f).roundToInt()
    }
    val scrollState = rememberScrollState(initialOffset)
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colors.background),
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scrollState = scrollState) }
    ) {
        Column(Modifier.fillMaxSize().padding(top = 28.dp)) {
            if (isWeek) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = GridPadding),
                    horizontalArrangement = Arrangement.spacedBy(GridSpacing)
                ) {
                    Spacer(Modifier.width(gutterWidth))
                    days.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f).height(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (day == today) MaterialTheme.colors.primary else Color.Transparent)
                                .clickable { onSelectDay(day) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault()),
                                color = if (day == today) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                Text(
                    days.single().getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.primary
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .rotaryScrollable(RotaryScrollableDefaults.behavior(scrollState), focusRequester)
                    .focusRequester(focusRequester).focusable()
                    .verticalScroll(scrollState)
                    .padding(start = GridPadding, end = GridPadding, top = 8.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimetableColumns(
                    entries, startMinutes, endMinutes, hourHeight, gutterWidth,
                    timetable.textColor.scheduleColor(), now, onSelectDay, onSelect
                )
                if (onShowOptions != null) {
                    Spacer(Modifier.height(12.dp))
                    ViewOptionsButton(onShowOptions)
                }
            }
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
private fun TimetableColumns(
    entries: Map<DayOfWeek, List<ScheduleEntry>>,
    startMinutes: Int,
    endMinutes: Int,
    hourHeight: Dp,
    gutterWidth: Dp,
    textColor: Color,
    now: LocalDateTime,
    onSelectDay: (DayOfWeek) -> Unit,
    onSelect: (ScheduleEntry) -> Unit
) {
    val isWeek = entries.size > 1
    val gridHeight = hourHeight * ((endMinutes - startMinutes) / 60f)
    val lineColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    val currentTimeColor = MaterialTheme.colors.primary
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(GridSpacing)) {
        Box(Modifier.width(gutterWidth).height(gridHeight)) {
            for (hour in startMinutes / 60..endMinutes / 60) {
                if (!isWeek || hour % 3 == 0) {
                    Text(
                        hour.toString(), fontSize = 10.sp,
                        color = MaterialTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth().offset(
                            y = (hourHeight * (hour - startMinutes / 60) - 7.dp)
                                .coerceIn(0.dp, gridHeight - 14.dp)
                        )
                    )
                }
            }
        }
        entries.forEach { (day, items) ->
            val dayLabel = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
            BoxWithConstraints(
                Modifier.weight(1f).height(gridHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.45f))
                    .drawBehind {
                        for (hour in startMinutes / 60..endMinutes / 60) {
                            val y = hourHeight.toPx() * (hour - startMinutes / 60)
                            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                        }
                    }
                    .drawWithContent {
                        drawContent()
                        val minutes = now.hour * 60 + now.minute
                        if (day == now.dayOfWeek && minutes in startMinutes until endMinutes) {
                            val y = hourHeight.toPx() * (minutes - startMinutes) / 60f
                            drawLine(currentTimeColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                            drawCircle(currentTimeColor, 2.dp.toPx(), Offset(2.dp.toPx(), y))
                        }
                    }
                    .then(if (isWeek) Modifier.clickable { onSelectDay(day) } else Modifier)
                    .semantics { contentDescription = dayLabel }
            ) {
                val dayWidth = maxWidth
                scheduleCells(items).forEach { cell ->
                    val time = cell.entry.classTime
                    val gap = minOf(GridSpacing, dayWidth / (cell.laneCount * 2))
                    val width = (dayWidth - gap * (cell.laneCount - 1)) / cell.laneCount
                    val height = (hourHeight * ((time.end - time.begin) / 60f) - 2.dp).coerceAtLeast(1.dp)
                    TimetableBlock(
                        entry = cell.entry,
                        textColor = textColor,
                        showDetails = !isWeek,
                        modifier = Modifier
                            .offset(x = (width + gap) * cell.lane, y = hourHeight * ((time.begin - startMinutes) / 60f))
                            .width(width).height(height),
                        onSelect = { if (isWeek) onSelectDay(day) else onSelect(cell.entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableBlock(
    entry: ScheduleEntry,
    textColor: Color,
    showDetails: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val time = formatTimeRange(entry.classTime.begin, entry.classTime.end)
    BoxWithConstraints(
        modifier.clip(RoundedCornerShape(4.dp))
            .background(entry.color.scheduleColor())
            .clickable(onClick = onSelect)
            .semantics(mergeDescendants = true) { contentDescription = "${entry.title}, $time, ${entry.location}" }
    ) {
        if (showDetails && maxHeight >= 20.dp) {
            val titleLines = if (maxHeight >= 40.dp) 2 else 1
            val showTime = maxHeight >= 64.dp && maxWidth >= 88.dp
            val showLocation = maxHeight >= 80.dp && maxWidth >= 88.dp && entry.location.isNotBlank()
            Column(Modifier.padding(5.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    entry.title, color = textColor,
                    fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = titleLines,
                    overflow = TextOverflow.Ellipsis
                )
                if (showTime) Text(time, color = textColor.copy(alpha = 0.85f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (showLocation) Text(
                    entry.location, color = textColor.copy(alpha = 0.85f),
                    fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Preview(device = WearDevices.LARGE_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WeekTimetableViewPreview() {
    SoapTheme { WeekTimetableView(Timetable.mock(), DayOfWeek.TUESDAY, LocalDateTime.of(2026, 9, 22, 11, 0)) {} }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Preview(device = WearDevices.LARGE_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DayTimetableViewPreview() {
    SoapTheme { DayTimetableView(Timetable.mock(), DayOfWeek.TUESDAY, LocalDateTime.of(2026, 9, 22, 11, 0)) {} }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimetableGridPreview() {
    SoapTheme {
        TimetableGrid(Timetable.mock(), DayOfWeek.entries.take(5), DayOfWeek.TUESDAY, LocalDateTime.of(2026, 9, 22, 11, 0), {}, {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 170, heightDp = 240)
@Composable
private fun TimetableColumnsPreview() {
    SoapTheme {
        TimetableColumns(
            mapOf(DayOfWeek.TUESDAY to Timetable.mock().scheduleEntries(DayOfWeek.TUESDAY)),
            600, 840, 60.dp, 24.dp, Color.White, LocalDateTime.of(2026, 9, 22, 11, 0), {}, {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 140, heightDp = 96)
@Composable
private fun TimetableBlockPreview() {
    SoapTheme {
        TimetableBlock(Timetable.mock().scheduleEntries(DayOfWeek.TUESDAY).first(), Color.White, true, Modifier.fillMaxSize()) {}
    }
}
