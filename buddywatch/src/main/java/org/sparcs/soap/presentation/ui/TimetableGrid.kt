package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.ScheduleEntry
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.scheduleCells
import org.sparcs.soap.data.models.scheduleEntries
import org.sparcs.soap.data.models.visibleDays
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.shared.scheduleColor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekTimetableView(
    timetable: Timetable,
    today: DayOfWeek,
    onShowOptions: (() -> Unit)? = null,
    onSelectDay: (DayOfWeek) -> Unit
) {
    TimetableGrid(timetable, timetable.visibleDays, today, onSelectDay, {}, onShowOptions)
}

@Composable
fun DayTimetableView(
    timetable: Timetable,
    day: DayOfWeek,
    onShowOptions: (() -> Unit)? = null,
    onSelect: (ScheduleEntry) -> Unit
) {
    if (timetable.scheduleEntries(day).isEmpty()) {
        ScheduleScaffold(day.getDisplayName(TextStyle.FULL, Locale.getDefault()), onShowOptions = onShowOptions) {
            item { Text(stringResource(R.string.no_schedule), textAlign = TextAlign.Center) }
        }
    } else {
        TimetableGrid(timetable, listOf(day), day, {}, onSelect, onShowOptions)
    }
}

@Composable
private fun TimetableGrid(
    timetable: Timetable,
    days: List<DayOfWeek>,
    today: DayOfWeek,
    onSelectDay: (DayOfWeek) -> Unit,
    onSelect: (ScheduleEntry) -> Unit,
    onShowOptions: (() -> Unit)? = null
) {
    val entries = days.associateWith { timetable.scheduleEntries(it) }
    val allEntries = entries.values.flatten()
    val startMinutes = minOf(540, (allEntries.minOfOrNull { it.classTime.begin } ?: 540) / 60 * 60)
    val endMinutes = maxOf(1080, ((allEntries.maxOfOrNull { it.classTime.end } ?: 1080) + 59) / 60 * 60)
    val isWeek = days.size > 1
    val hourHeight = if (isWeek) 32.dp else 48.dp
    val gridHeight = hourHeight * ((endMinutes - startMinutes) / 60f)
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway(scrollState)) },
        positionIndicator = { PositionIndicator(scrollState = scrollState) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .rotaryScrollable(RotaryScrollableDefaults.behavior(scrollState), focusRequester)
                .focusRequester(focusRequester).focusable()
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (isWeek) stringResource(R.string.view_week)
                else today.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.caption1,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isWeek) Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(20.dp))
                days.forEach { day ->
                    Text(
                        day.getDisplayName(TextStyle.NARROW_STANDALONE, Locale.getDefault()),
                        modifier = Modifier.weight(1f).clickable { onSelectDay(day) },
                        textAlign = TextAlign.Center,
                        color = if (day == today) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.width(20.dp).height(gridHeight)) {
                    for (hour in startMinutes / 60..endMinutes / 60) {
                        if (!isWeek || hour % 3 == 0) Text(
                            hour.toString(), fontSize = 9.sp,
                            modifier = Modifier.offset(y = (hourHeight * (hour - startMinutes / 60)).coerceAtMost(gridHeight - 12.dp))
                        )
                    }
                }
                days.forEach { day ->
                    BoxWithConstraints(
                        Modifier.weight(1f).height(gridHeight).padding(horizontal = 1.dp)
                            .background(MaterialTheme.colors.surface, RoundedCornerShape(4.dp))
                            .then(if (isWeek) Modifier.clickable { onSelectDay(day) } else Modifier)
                            .semantics { contentDescription = day.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
                    ) {
                        val dayWidth = maxWidth
                        scheduleCells(entries.getValue(day)).forEach { cell ->
                            val time = cell.entry.classTime
                            val width = (dayWidth - 2.dp * (cell.laneCount - 1)) / cell.laneCount
                            Box(
                                Modifier.offset(x = (width + 2.dp) * cell.lane, y = hourHeight * ((time.begin - startMinutes) / 60f))
                                    .width(width).height((hourHeight * ((time.end - time.begin) / 60f) - 2.dp).coerceAtLeast(1.dp))
                                    .background(cell.entry.color.scheduleColor(), RoundedCornerShape(4.dp))
                                    .then(if (isWeek) Modifier else Modifier.clickable { onSelect(cell.entry) })
                            ) {
                                if (!isWeek) Column(Modifier.padding(4.dp)) {
                                    Text(cell.entry.title, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = timetable.textColor.scheduleColor())
                                    Text(cell.entry.location, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = timetable.textColor.scheduleColor())
                                }
                            }
                        }
                    }
                }
            }
            if (onShowOptions != null) ViewOptionsButton(onShowOptions)
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun WeekTimetableViewPreview() {
    SoapTheme { WeekTimetableView(Timetable.mock(), LocalDate.now().dayOfWeek) {} }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DayTimetableViewPreview() {
    SoapTheme { DayTimetableView(Timetable.mock(), DayOfWeek.MONDAY) {} }
}

@Preview(device = WearDevices.LARGE_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimetableGridPreview() {
    SoapTheme { TimetableGrid(Timetable.mock(), DayOfWeek.entries.take(5), DayOfWeek.MONDAY, {}, {}) }
}
