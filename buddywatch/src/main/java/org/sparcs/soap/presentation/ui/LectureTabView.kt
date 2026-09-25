package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.ScheduleEntry
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.defaultSelection
import org.sparcs.soap.data.models.scheduleEntries
import org.sparcs.soap.presentation.theme.SoapTheme
import org.sparcs.soap.shared.formatTimeRange
import org.sparcs.soap.shared.scheduleColor
import org.sparcs.soap.shared.statusString
import java.time.DayOfWeek
import java.time.LocalDateTime

@Composable
fun LectureTabView(
    items: List<ScheduleEntry>,
    initialSelection: String?,
    now: LocalDateTime,
    title: String = stringResource(R.string.up_next),
    onShowOptions: (() -> Unit)? = null
) {
    val selectedID = initialSelection ?: defaultSelection(items, now)
    val initialIndex = items.indexOfFirst { it.id == selectedID }.coerceAtLeast(0)
    val state = rememberScalingLazyListState(initialCenterItemIndex = initialIndex + 1)
    ScheduleScaffold(title, state, onShowOptions) {
        if (items.isEmpty()) item {
            Text(stringResource(R.string.no_schedule), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        items(items, key = { it.id }) { entry -> LectureView(entry, now) }
    }
}

@Composable
fun LectureView(entry: ScheduleEntry, now: LocalDateTime) {
    Column(
        modifier = Modifier.fillMaxWidth().background(entry.color.scheduleColor().copy(alpha = 0.18f), MaterialTheme.shapes.medium).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(entry.title, style = MaterialTheme.typography.title3, fontWeight = FontWeight.Bold)
        Text(formatTimeRange(entry.classTime.begin, entry.classTime.end), style = MaterialTheme.typography.caption1)
        Text(entry.classTime.statusString(LocalContext.current, now), color = entry.color.scheduleColor(), style = MaterialTheme.typography.caption1)
        Text(entry.location, style = MaterialTheme.typography.caption2)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LectureTabViewPreview() {
    SoapTheme { LectureTabView(Timetable.mock().scheduleEntries(DayOfWeek.MONDAY), null, LocalDateTime.of(2026, 9, 21, 9, 0)) }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LectureViewPreview() {
    SoapTheme { LectureView(Timetable.mock().scheduleEntries(DayOfWeek.MONDAY).first(), LocalDateTime.of(2026, 9, 21, 9, 0)) }
}
