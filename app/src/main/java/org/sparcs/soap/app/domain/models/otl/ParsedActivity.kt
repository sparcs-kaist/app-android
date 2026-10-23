package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.DayType

data class ParsedActivity(
    val title: String? = null,
    val location: String? = null,
    val days: List<DayType> = emptyList(),
    val begin: Int? = null,
    val end: Int? = null,
    val laterTimes: List<Time> = emptyList(),
) {
    constructor(
        title: String? = null,
        location: String? = null,
        day: DayType?,
        begin: Int? = null,
        end: Int? = null,
    ) : this(title, location, listOfNotNull(day), begin, end)

    data class Time(val begin: Int, val end: Int? = null)

    val day: DayType? get() = days.firstOrNull()

    val isEmpty: Boolean get() = title == null && location == null && days.isEmpty() && begin == null

    fun applied(draft: ActivityDraft): ActivityDraft {
        val current = draft.times.first()
        val length = current.duration.coerceAtLeast(MINIMUM_LENGTH)
        val first = if (begin == null) Time(current.begin, current.end) else Time(begin, end)
        val hours = listOf(first) + laterTimes
        val meetingDays = days.map { it.value }.ifEmpty { listOf(current.day) }
        val ranges = meetingDays.flatMap { day -> hours.map { range(day, it, length) } }.distinct()
        val times = if (ranges.size > 1) ranges else listOf(ranges.first()) + draft.times.drop(1)
        return draft.copy(
            title = title ?: draft.title,
            location = location ?: draft.location,
            times = times,
        )
    }

    private fun range(day: Int, hour: Time, length: Int): ActivityTime {
        val start = snapped(hour.begin).coerceAtMost(MINUTES_IN_DAY - MINIMUM_LENGTH)
        val finish = (hour.end?.let(::snapped) ?: (start + length)).coerceIn(start + MINIMUM_LENGTH, MINUTES_IN_DAY)
        return ActivityTime(day, start, finish)
    }

    companion object {
        private const val MINUTES_IN_DAY = 24 * 60
        private const val MINIMUM_LENGTH = 15

        fun fromClock(
            title: String?,
            location: String?,
            days: List<DayType> = emptyList(),
            startHour: Int?,
            startMinute: Int?,
            endHour: Int?,
            endMinute: Int?,
            durationMinutes: Int?,
        ): ParsedActivity {
            val begin = minutes(startHour, startMinute)
            var end = minutes(endHour, endMinute)
            if (begin != null && end != null && end <= begin) {
                val later = end + 12 * 60
                end = later.takeIf { it > begin && it <= MINUTES_IN_DAY }
            }
            if (end == null && begin != null && durationMinutes != null && durationMinutes > 0) {
                end = minOf(begin + durationMinutes, MINUTES_IN_DAY)
            }
            return ParsedActivity(
                title = cleaned(title),
                location = cleaned(location),
                days = days,
                begin = begin,
                end = if (begin == null) null else end,
            )
        }

        private fun minutes(hour: Int?, minute: Int?): Int? {
            if (hour == null || hour !in 0..23) return null
            return hour * 60 + (minute?.coerceIn(0, 59) ?: 0)
        }

        private fun snapped(minutes: Int): Int = ActivityDraft.snap(minutes.toFloat()).coerceIn(0, MINUTES_IN_DAY)

        private fun cleaned(text: String?): String? = text?.trim()?.takeIf { it.isNotEmpty() }
    }
}

enum class ActivityParsingError { UNAVAILABLE, UNSAFE_REQUEST, NOTHING_FOUND, FAILED }

class ActivityParsingException(val reason: ActivityParsingError) : Exception()
