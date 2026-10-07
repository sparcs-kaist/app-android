package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.shared.views.contentViews.getTagChipColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LectureTimeFilterRow(time: LectureTimeFilter, onTimeChange: (LectureTimeFilter) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = time.day == null,
                onClick = { onTimeChange(time.copy(day = null)) },
                label = { Text(stringResource(R.string.lecture_time_any)) },
                colors = getTagChipColors(),
            )
            DayType.entries.sortedBy { it.value }.forEach { day ->
                FilterChip(
                    selected = time.day == day,
                    onClick = { onTimeChange(time.copy(day = day)) },
                    label = { Text(stringResource(day.stringValue)) },
                    colors = getTagChipColors(),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeField(
                label = stringResource(R.string.lecture_time_begin),
                value = time.begin,
                options = LectureTimeFilter.selectableTimes.dropLast(1),
                modifier = Modifier.weight(1f),
            ) { begin ->
                onTimeChange(time.copy(begin = begin, end = time.end?.let { if (begin != null && it <= begin) begin + 30 else it }))
            }
            TimeField(
                label = stringResource(R.string.lecture_time_end),
                value = time.end,
                options = LectureTimeFilter.selectableTimes.drop(1),
                modifier = Modifier.weight(1f),
            ) { end ->
                onTimeChange(time.copy(end = end, begin = time.begin?.let { if (end != null && it >= end) end - 30 else it }))
            }
        }
    }
}

@Composable
private fun TimeField(label: String, value: Int?, options: List<Int>, modifier: Modifier, onChange: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(value?.let { "%02d:%02d".format(it / 60, it % 60) } ?: stringResource(R.string.lecture_time_any), Modifier.weight(1f))
                Icon(Icons.Rounded.ExpandMore, null)
            }
            DropdownMenu(expanded, { expanded = false }, modifier = Modifier.heightIn(max = 320.dp), containerColor = MaterialTheme.colorScheme.background) {
                DropdownMenuItem(text = { Text(stringResource(R.string.lecture_time_any)) }, onClick = { onChange(null); expanded = false })
                options.forEach { minutes ->
                    DropdownMenuItem(
                        text = { Text("%02d:%02d".format(minutes / 60, minutes % 60)) },
                        onClick = { onChange(minutes); expanded = false },
                    )
                }
            }
        }
    }
}
