package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.DayType
import kotlin.math.floor

internal object TimetableRangeSelection {
    const val start = 480
    const val end = 1380
    const val step = 30
    private val days = DayType.weekdays()

    fun range(dayFraction: Float, startFraction: Float, endFraction: Float): LectureTimeFilter {
        fun cell(fraction: Float) = (start + floor(fraction * (end - start) / step).toInt() * step)
            .coerceIn(start, end - step)
        val first = cell(startFraction)
        val last = cell(endFraction)
        return LectureTimeFilter(
            days[floor(dayFraction * days.size).toInt().coerceIn(days.indices)],
            minOf(first, last), maxOf(first, last) + step,
        )
    }

    fun normalized(filter: LectureTimeFilter): LectureTimeFilter {
        val begin = (filter.begin ?: if (filter.isEmpty) 540 else start).coerceIn(start, end - step)
        return LectureTimeFilter(
            filter.day?.takeIf { it in days } ?: days.first(), begin,
            (filter.end ?: if (filter.isEmpty) 600 else end).coerceIn(begin + step, end),
        )
    }

    fun move(filter: LectureTimeFilter, delta: Int): LectureTimeFilter {
        val block = normalized(filter)
        val length = block.end!! - block.begin!!
        val begin = (block.begin + delta).coerceIn(start, end - length)
        return block.copy(begin = begin, end = begin + length)
    }

    fun moveDay(filter: LectureTimeFilter, delta: Int): LectureTimeFilter {
        val block = normalized(filter)
        return block.copy(day = days[(days.indexOf(block.day) + delta).coerceIn(days.indices)])
    }

    fun extend(filter: LectureTimeFilter): LectureTimeFilter {
        val block = normalized(filter)
        return block.copy(end = if (block.end!! + step <= end) block.end + step else block.begin!! + step)
    }
}
