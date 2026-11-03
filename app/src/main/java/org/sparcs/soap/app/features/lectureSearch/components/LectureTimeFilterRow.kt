package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDropdownMenuItem
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
import org.sparcs.soap.app.shared.formatters.formattedTime
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LectureTimeFilterRow(time: LectureTimeFilter, onTimeChange: (LectureTimeFilter) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = time.day == null,
                onClick = { onTimeChange(time.copy(day = null)) },
                label = { Text(stringResource(R.string.lecture_time_any)) },
            )
            DayType.entries.sortedBy { it.value }.forEach { day ->
                FilterChip(
                    selected = time.day == day,
                    onClick = { onTimeChange(time.copy(day = day)) },
                    label = { Text(stringResource(day.stringValue)) },
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

/** Read-only dropdown field for a begin or end time, with "Any" first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(label: String, value: Int?, options: List<Int>, modifier: Modifier, onChange: (Int?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = value?.let { formattedTime(it) } ?: stringResource(R.string.lecture_time_any),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 320.dp),
            shape = MenuDefaults.standaloneGroupShape,
            containerColor = MenuDefaults.groupStandardContainerColor,
        ) {
            val count = options.size + 1
            val checkIcon: @Composable () -> Unit = {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
            }
            SelectableDropdownMenuItem(
                colors = selectableMenuItemColors(),
                selected = value == null,
                onClick = { onChange(null); expanded = false },
                text = { Text(stringResource(R.string.lecture_time_any)) },
                shapes = MenuDefaults.itemShape(0, count),
                selectedLeadingIcon = checkIcon,
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
            options.forEachIndexed { index, minutes ->
                SelectableDropdownMenuItem(
                    colors = selectableMenuItemColors(),
                    selected = value == minutes,
                    onClick = { onChange(minutes); expanded = false },
                    text = { Text(formattedTime(minutes)) },
                    shapes = MenuDefaults.itemShape(index + 1, count),
                    selectedLeadingIcon = checkIcon,
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
