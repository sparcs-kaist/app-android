package org.sparcs.soap.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.ScheduleEntry
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.scheduleEntries
import org.sparcs.soap.presentation.theme.SoapTheme
import java.time.DayOfWeek

@Composable
fun TimetableList(
    items: List<ScheduleEntry>,
    title: String = stringResource(R.string.view_list),
    onShowOptions: (() -> Unit)? = null,
    onSelect: (ScheduleEntry) -> Unit
) {
    ScheduleScaffold(title, onShowOptions = onShowOptions) {
        if (items.isEmpty()) item {
            Text(stringResource(R.string.no_schedule), textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        }
        items(items, key = { it.id }) { entry ->
            LectureItem(entry) { onSelect(entry) }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TimetableListPreview() {
    SoapTheme { TimetableList(Timetable.mock().scheduleEntries(DayOfWeek.MONDAY)) {} }
}
