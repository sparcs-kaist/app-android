package org.sparcs.soap.app.features.lectureSearch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.shared.formatters.formattedTime
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors
import org.sparcs.soap.app.theme.ui.Theme

/**
 * The class time filter: the day as a Material 3 Expressive connected button group, then the
 * earliest start and latest end as a segmented group of rows that open the time menus.
 * [onChooseOnTimetable], when set, adds a last row that picks the range on the timetable instead.
 */
@Composable
fun LectureTimeFilterRow(
    time: LectureTimeFilter,
    onTimeChange: (LectureTimeFilter) -> Unit,
    onChooseOnTimetable: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DayButtonGroup(time.day) { onTimeChange(time.copy(day = it)) }

        val count = if (onChooseOnTimetable != null) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            TimeRow(
                label = stringResource(R.string.lecture_time_begin),
                value = time.begin,
                options = LectureTimeFilter.selectableTimes.dropLast(1),
                index = 0,
                count = count,
            ) { begin ->
                onTimeChange(time.copy(begin = begin, end = time.end?.let { if (begin != null && it <= begin) begin + 30 else it }))
            }
            TimeRow(
                label = stringResource(R.string.lecture_time_end),
                value = time.end,
                options = LectureTimeFilter.selectableTimes.drop(1),
                index = 1,
                count = count,
            ) { end ->
                onTimeChange(time.copy(end = end, begin = time.begin?.let { if (end != null && it >= end) end - 30 else it }))
            }
            if (onChooseOnTimetable != null) {
                SegmentedListItem(
                    onClick = onChooseOnTimetable,
                    shapes = ListItemDefaults.segmentedShapes(2, count),
                    colors = sheetSegmentedColors(),
                    leadingContent = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                ) { Text(stringResource(R.string.choose_time_on_timetable)) }
            }
        }
    }
}

/**
 * Monday to Sunday as one connected group of toggle buttons. Tapping a day selects it; tapping
 * the selected day again clears it back to any day.
 */
@Composable
private fun DayButtonGroup(selected: DayType?, onSelect: (DayType?) -> Unit) {
    val days = DayType.entries.sortedBy { it.value }
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = Modifier.fillMaxWidth()
    ) {
        days.forEachIndexed { index, day ->
            ToggleButton(
                checked = selected == day,
                onCheckedChange = { onSelect(if (it) day else null) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    days.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                // The rows' container, as the default would vanish into the bottom sheet.
                colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                // Seven days share a phone's width, so keep the side padding small.
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
            ) {
                Text(stringResource(day.stringValue), maxLines = 1, overflow = TextOverflow.Clip)
            }
        }
    }
}

/** A start or end time as a segmented row; tapping it opens the times, with "Any" first. */
@Composable
private fun TimeRow(
    label: String,
    value: Int?,
    options: List<Int>,
    index: Int,
    count: Int,
    onChange: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    SegmentedListItem(
        onClick = { expanded = true },
        shapes = ListItemDefaults.segmentedShapes(index, count),
        colors = sheetSegmentedColors(),
        trailingContent = {
            Box {
                Text(
                    value?.let { formattedTime(it) } ?: stringResource(R.string.lecture_time_any),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExpressiveMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    val total = options.size + 1
                    val checkIcon: @Composable () -> Unit = {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(MenuDefaults.LeadingIconSize))
                    }
                    MenuGroup(index = 0, count = 1, maxHeight = 320.dp) {
                        SelectableDropdownMenuItem(
                            colors = selectableMenuItemColors(),
                            selected = value == null,
                            onClick = { onChange(null); expanded = false },
                            text = { Text(stringResource(R.string.lecture_time_any)) },
                            shapes = MenuDefaults.itemShape(0, total),
                            selectedLeadingIcon = checkIcon,
                        )
                        options.forEachIndexed { i, minutes ->
                            SelectableDropdownMenuItem(
                                colors = selectableMenuItemColors(),
                                selected = value == minutes,
                                onClick = { onChange(minutes); expanded = false },
                                text = { Text(formattedTime(minutes)) },
                                shapes = MenuDefaults.itemShape(i + 1, total),
                                selectedLeadingIcon = checkIcon,
                            )
                        }
                    }
                }
            }
        },
    ) { Text(label) }
}

/** One step above the bottom sheet's own container, so the rows stand out on it. */
@Composable
private fun sheetSegmentedColors() =
    ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        LectureTimeFilterRow(LectureTimeFilter(day = DayType.TUE, begin = 600), {}, onChooseOnTimetable = {})
    }
}
