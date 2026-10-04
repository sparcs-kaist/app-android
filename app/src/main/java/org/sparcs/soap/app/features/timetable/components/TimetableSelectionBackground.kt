package org.sparcs.soap.app.features.timetable.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.theme.ui.LocalTimetableTheme
import kotlin.math.roundToInt

@Composable
internal fun TimetableSelectionBackground(
    timetable: Timetable?,
    days: List<Int>,
    minutePx: Float,
    insetPx: Float,
    gutter: Float,
    trailing: Float,
    dayWidth: Float,
    modifier: Modifier = Modifier,
    excludingID: Int? = null,
    startMinute: Int = 0,
    endMinute: Int = 1440,
) {
    val density = LocalDensity.current
    val outline =
        LocalTimetableTheme.current.separatorColor ?: MaterialTheme.colorScheme.outlineVariant
    Box(modifier.clipToBounds()) {
        Canvas(Modifier.fillMaxSize()) {
            for (hour in startMinute / 60..endMinute / 60) {
                val lineY = insetPx + (hour * 60 - startMinute) * minutePx
                drawLine(
                    outline.copy(alpha = .6f),
                    Offset(gutter, lineY),
                    Offset(size.width - trailing, lineY),
                    1.dp.toPx()
                )
            }
        }
        for (hour in startMinute / 60..endMinute / 60) Text(
            "%02d".format(hour),
            Modifier
                .offset {
                    IntOffset(
                        0,
                        (insetPx + (hour * 60 - startMinute) * minutePx - 8.dp.toPx()).roundToInt()
                    )
                }
                .width(36.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = LocalTimetableTheme.current.gridLabelColor
                ?: MaterialTheme.colorScheme.onSurfaceVariant)
        days.forEachIndexed { index, value ->
            timetable?.getLectures(DayType.fromValue(value)!!, null)
                ?.filter { it.lectureClass.end > startMinute && it.lectureClass.begin < endMinute }
                ?.forEach { item ->
                    val h = with(density) {
                        ((minOf(item.lectureClass.end, endMinute) - maxOf(
                            item.lectureClass.begin,
                            startMinute
                        )) * minutePx - 3).coerceAtLeast(1f)
                            .toDp()
                    }
                    TimetableGridCell(
                        item, false, h, Modifier
                            .offset {
                                IntOffset(
                                    (gutter + index * dayWidth + 2).roundToInt(),
                                    (insetPx + (maxOf(
                                        item.lectureClass.begin,
                                        startMinute
                                    ) - startMinute) * minutePx).roundToInt()
                                )
                            }
                            .width(with(density) { (dayWidth - 4).toDp() })
                    )
                }
            timetable?.activities.orEmpty()
                .filter { it.day == value && it.id != excludingID && it.end > startMinute && it.begin < endMinute }
                .forEach { activity ->
                    Surface(
                        color = LocalTimetableTheme.current.colorFor(activity.id),
                        contentColor = LocalTimetableTheme.current.textColor,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (gutter + index * dayWidth + 2).roundToInt(),
                                    (insetPx + (maxOf(
                                        activity.begin,
                                        startMinute
                                    ) - startMinute) * minutePx).roundToInt()
                                )
                            }
                            .size(
                                with(density) { (dayWidth - 4).toDp() },
                                with(density) {
                                    ((minOf(activity.end, endMinute) - maxOf(
                                        activity.begin,
                                        startMinute
                                    )) * minutePx - 3).coerceAtLeast(
                                        1f
                                    ).toDp()
                                })
                    ) {
                        Text(
                            activity.title,
                            Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
        }
    }
}

@Composable
internal fun TimetableSelectionDayHeader(days: List<Int>) {
    Row(Modifier
        .fillMaxWidth()
        .padding(start = 44.dp, end = 16.dp, bottom = 12.dp)) {
        days.forEach { day ->
            Text(
                stringResource(DayType.fromValue(day)!!.stringValue),
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = LocalTimetableTheme.current.gridLabelColor
                    ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
