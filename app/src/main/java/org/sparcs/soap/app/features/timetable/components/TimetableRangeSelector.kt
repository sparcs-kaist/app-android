package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureTimeFilter
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import kotlin.math.abs
import org.sparcs.soap.app.domain.models.otl.TimetableRangeSelection as Selection

@Composable
fun TimetableRangeSelector(
    timetable: Timetable?,
    filter: LectureTimeFilter,
    onFilterChange: (LectureTimeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf<LectureTimeFilter?>(null) }
    val onChange by rememberUpdatedState(onFilterChange)
    val shown = draft ?: filter
    val days = DayType.weekdays()
    val label = stringResource(R.string.lecture_time_filter)
    val hint = stringResource(R.string.time_range_hint)
    val anyTime = stringResource(R.string.lecture_time_any)
    val actionNames = listOf(
        R.string.time_range_earlier, R.string.time_range_later,
        R.string.time_range_previous_day, R.string.time_range_next_day, R.string.time_range_longer,
    ).map { stringResource(it) }
    val description = if (shown.isEmpty) anyTime else listOfNotNull(
        shown.day?.let { stringResource(it.fullStringValue) },
        "${clockTime(shown.begin ?: Selection.start)}\u2013${clockTime(shown.end ?: Selection.end)}",
    ).joinToString(", ")
    val accent = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val selectionBase = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    val selectionText = MaterialTheme.colorScheme.onSurface
    val selectionTextStyle = MaterialTheme.typography.labelSmall

    Column(
        modifier
            .background(
                LocalTimetableTheme.current.backgroundColor ?: MaterialTheme.colorScheme.background
            )
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        TimetableSelectionDayHeader(days.map { it.value })
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val leftPx = with(density) { 44.dp.toPx() }
            val topPx = with(density) { 24.dp.toPx() }
            val widthPx = with(density) { maxWidth.toPx() - 16.dp.toPx() }
            val heightPx = with(density) { maxHeight.toPx() - 24.dp.toPx() }
            TimetableSelectionBackground(
                timetable = timetable,
                days = days.map { it.value },
                minutePx = (heightPx - topPx).coerceAtLeast(1f) / (Selection.end - Selection.start),
                insetPx = topPx,
                gutter = leftPx,
                trailing = with(density) { 16.dp.toPx() },
                dayWidth = (widthPx - leftPx).coerceAtLeast(1f) / days.size,
                startMinute = Selection.start,
                endMinute = Selection.end,
                modifier = Modifier.fillMaxSize(),
            )
            Canvas(
                Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "$label. $hint"
                        stateDescription = description
                        customActions = listOf(
                            CustomAccessibilityAction(actionNames[0]) {
                                onChange(Selection.move(filter, -30)); true
                            },
                            CustomAccessibilityAction(actionNames[1]) {
                                onChange(Selection.move(filter, 30)); true
                            },
                            CustomAccessibilityAction(actionNames[2]) {
                                onChange(Selection.moveDay(filter, -1)); true
                            },
                            CustomAccessibilityAction(actionNames[3]) {
                                onChange(Selection.moveDay(filter, 1)); true
                            },
                            CustomAccessibilityAction(actionNames[4]) {
                                onChange(
                                    Selection.extend(
                                        filter
                                    )
                                ); true
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        val left = 44.dp.toPx()
                        val top = 24.dp.toPx()
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val origin = down.position
                            if (origin.x < left || origin.x > size.width - 16.dp.toPx() || origin.y < top) return@awaitEachGesture
                            var verticalDrag = false
                            var rejected = false
                            fun range(point: Offset) = Selection.range(
                                (origin.x - left) / (size.width - left - 16.dp.toPx()).coerceAtLeast(
                                    1f
                                ),
                                (origin.y - top) / (size.height - top - 24.dp.toPx()).coerceAtLeast(
                                    1f
                                ),
                                (point.y - top) / (size.height - top - 24.dp.toPx()).coerceAtLeast(
                                    1f
                                ),
                            )
                            try {
                                draft = range(origin)
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change =
                                        event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.isConsumed || event.changes.count { it.pressed } > 1) break
                                    val delta = change.position - origin
                                    if (!verticalDrag && !rejected && delta.getDistance() > viewConfiguration.touchSlop) {
                                        verticalDrag = abs(delta.y) >= abs(delta.x)
                                        rejected = !verticalDrag
                                    }
                                    val alongDay = !rejected
                                    draft = if (alongDay) range(change.position) else null
                                    if (!change.pressed) {
                                        if (alongDay) onChange(range(change.position))
                                        break
                                    }
                                    if (alongDay && abs(delta.y) > viewConfiguration.touchSlop) change.consume()
                                }
                            } finally {
                                draft = null
                            }
                        }
                    }
            ) {
                val left = 44.dp.toPx()
                val top = 24.dp.toPx()
                val width = (size.width - left - 16.dp.toPx()).coerceAtLeast(1f)
                val height = (size.height - top - 24.dp.toPx()).coerceAtLeast(1f)
                val column = width / 5
                fun y(minutes: Int) = top + (minutes.coerceIn(
                    Selection.start,
                    Selection.end
                ) - Selection.start) / 900f * height

                if (!shown.isEmpty && (shown.day == null || shown.day in days)) {
                    val begin = shown.begin ?: Selection.start
                    val end = shown.end ?: Selection.end
                    val position = Offset(left + (shown.day?.value ?: 0) * column + 2, y(begin))
                    val blockSize = Size(
                        (if (shown.day == null) width else column) - 4,
                        (y(end) - y(begin)).coerceAtLeast(0f)
                    )
                    val radius = CornerRadius(6.dp.toPx())
                    drawRoundRect(selectionBase, position, blockSize, radius)
                    drawRoundRect(accent.copy(alpha = 0.24f), position, blockSize, radius)
                    drawRoundRect(
                        accent,
                        position,
                        blockSize,
                        radius,
                        style = Stroke(1.5.dp.toPx())
                    )
                    if (blockSize.height > 40.dp.toPx()) {
                        drawText(
                            textMeasurer,
                            "${clockTime(begin)}\n${clockTime(end)}",
                            topLeft = position + Offset(6.dp.toPx(), 6.dp.toPx()),
                            style = selectionTextStyle.copy(color = selectionText),
                            size = blockSize,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

private fun clockTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
