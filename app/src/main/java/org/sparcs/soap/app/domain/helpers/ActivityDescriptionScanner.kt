package org.sparcs.soap.app.domain.helpers

import org.sparcs.soap.app.domain.enums.otl.DayType
import java.util.regex.MatchResult
import java.util.regex.Pattern

data class ScannedTime(val begin: Int, val end: Int?)

object ActivityDescriptionScanner {
    fun day(text: String, today: DayType): DayType? = days(text, today).firstOrNull()

    fun days(text: String, today: DayType): List<DayType> {
        val found = mutableListOf<Pair<Int, List<DayType>>>()
        koreanDay.matches(text).forEach { match ->
            koreanDays[match.group(1)]?.let { found += match.start() to listOf(it) }
        }
        koreanDayRun.matches(text).forEach { match ->
            found += match.start() to match.group(1).mapNotNull { koreanDays[it.toString()] }
        }
        englishDay.matches(text).forEach { match ->
            englishDays[match.group(1).lowercase()]?.let { found += match.start() to listOf(it) }
        }
        relativeDay.matches(text).forEach { match ->
            val isTomorrow = match.group(1).lowercase() in setOf("내일", "tomorrow")
            found += match.start() to listOf(if (isTomorrow) today.next else today)
        }
        dayGroups.forEach { (expression, days) ->
            expression.matches(text).forEach { found += it.start() to days }
        }
        return found.sortedBy { it.first }.flatMap { it.second }.distinct()
    }

    fun times(text: String): ScannedTime? = allTimes(text).firstOrNull()

    fun allTimes(text: String): List<ScannedTime> {
        val expanded = expandingSharedHours(text)
        val found = mutableListOf<Pair<Int, ScannedTime>>()
        val rangeSpans = mutableListOf<IntRange>()

        timeRange.matches(expanded).forEach { match ->
            val start = Token.from(match, 1) ?: return@forEach
            val end = Token.from(match, 9) ?: return@forEach
            if (!start.isTimeLike && !end.isTimeLike) return@forEach
            val resolved = resolve(start, end) ?: return@forEach
            found += match.start() to resolved
            rangeSpans += match.start() until match.end()
        }
        singleTime.matches(expanded).forEach { match ->
            if (rangeSpans.any { match.start() in it }) return@forEach
            val token = Token.from(match, 1)?.takeIf { it.isTimeLike } ?: return@forEach
            var hour = token.hour24()?.takeIf { it < 24 } ?: return@forEach
            if (token.meridiem == null && hour in 1..6) hour += 12
            found += match.start() to ScannedTime(hour * 60 + token.minute, null)
        }
        return found.sortedBy { it.first }.map { it.second }.distinct()
    }

    private fun expandingSharedHours(text: String): String {
        var current = text
        repeat(4) {
            val expanded = sharedHour.matcher(current).replaceAll("$1 $3, $2$3")
            if (expanded == current) return current
            current = expanded
        }
        return current
    }

    private enum class Meridiem { AM, PM }

    private data class Token(val meridiem: Meridiem?, val hour: Int, val minute: Int, val isTimeLike: Boolean) {
        fun hour24(assumed: Meridiem? = null): Int? {
            if (hour !in 0..24) return null
            return when (meridiem ?: assumed) {
                Meridiem.PM -> if (hour < 12) hour + 12 else hour
                Meridiem.AM -> if (hour == 12) 0 else hour
                null -> hour
            }
        }

        companion object {
            fun from(match: MatchResult, first: Int): Token? {
                val group = { offset: Int -> match.group(first + offset) }
                val hour = group(1)?.toIntOrNull() ?: return null
                val colonMinute = group(2)
                val si = group(3)
                val koreanMinute = group(4)
                val half = group(5)
                val latinMeridiem = group(6)
                val koreanMeridiem = group(0)
                val minute = minOf((colonMinute ?: koreanMinute)?.toIntOrNull() ?: if (half == null) 0 else 30, 59)
                val meridiem = when {
                    latinMeridiem != null -> if (latinMeridiem.lowercase() == "p") Meridiem.PM else Meridiem.AM
                    koreanMeridiem != null -> if (koreanMeridiem in setOf("오전", "아침", "새벽")) Meridiem.AM else Meridiem.PM
                    else -> null
                }
                return Token(meridiem, hour, minute, meridiem != null || colonMinute != null || si != null)
            }
        }
    }

    private fun resolve(start: Token, end: Token): ScannedTime? {
        var startHour = start.hour24() ?: return null
        var endHour = end.hour24() ?: return null

        if (start.meridiem == null && end.meridiem == Meridiem.PM && startHour < 12 && startHour + 12 < endHour) {
            startHour += 12
        }
        if (end.meridiem == null && (start.meridiem == Meridiem.PM || startHour >= 12) && endHour < 12 && endHour + 12 > startHour) {
            endHour += 12
        }
        if (start.meridiem == null && end.meridiem == null && startHour in 1..6 && endHour <= 12) {
            startHour += 12
            endHour += 12
        }

        val begin = startHour * 60 + start.minute
        val endMinutes = endHour * 60 + end.minute
        if (startHour >= 24 || begin >= endMinutes || endMinutes > MINUTES_IN_DAY) {
            return if (startHour < 24) ScannedTime(begin, null) else null
        }
        return ScannedTime(begin, endMinutes)
    }

    private const val MINUTES_IN_DAY = 24 * 60

    private const val TIME =
        """(?:(오전|오후|저녁|밤|아침|새벽|낮)\s*)?(?<![A-Za-z0-9])(\d{1,2})(?!\d)(?:\s*:\s*(\d{2})|\s*(시)(?!간)(?:\s*(\d{1,2})\s*분|\s*(반))?)?(?:\s*([apAP])\.?\s*[mM]\b\.?)?"""

    private val timeRange = Pattern.compile(
        TIME + """\s*(-|–|—|~|〜|to|until|till|부터)\s*""" + TIME,
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )
    private val singleTime = Pattern.compile(TIME, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)

    private val sharedHour: Pattern = run {
        val meridiem = "오전|오후|저녁|밤|아침|새벽|낮"
        val clock = """\d{1,2}(?:\s*:\s*\d{2}|\s*시(?!간)(?:\s*\d{1,2}\s*분|\s*반)?)?"""
        Pattern.compile(
            "($meridiem)\\s*(?:,|및|과|와|이랑|랑|하고|그리고|/)?\\s*((?:$meridiem)\\s*)" +
                "($clock(?:\\s*(?:-|–|—|~|〜|부터)\\s*$clock)?)"
        )
    }

    private val koreanDay = Pattern.compile("""([월화수목금토일])요일""")
    private val englishDay = Pattern.compile(
        """\b((?i:monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tues|tue|weds|wed|thurs|thur|thu|fri)|Sat|Sun)s?\b"""
    )
    private val relativeDay = Pattern.compile("""(오늘|내일|(?i:today|tomorrow))""")
    private val koreanDayRun = Pattern.compile(
        """(?<![가-힣])([월화수목금토일](?:\s*[,/·]?\s*[월화수목금토일])+)(?![가-힣])"""
    )
    private val week = listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI, DayType.SAT, DayType.SUN)
    private val dayGroups: List<Pair<Pattern, List<DayType>>> = listOf(
        Pattern.compile("""매일|날마다|(?i:\bevery\s*day\b|\bdaily\b)""") to week,
        Pattern.compile("""평일|주중|(?i:\bweekdays\b|\bevery\s+weekday\b)""") to DayType.weekdays(),
        Pattern.compile("""주말|(?i:\bweekends?\b)""") to listOf(DayType.SAT, DayType.SUN),
    )

    private fun Pattern.matches(text: String): List<MatchResult> {
        val matcher = matcher(text)
        return generateSequence { if (matcher.find()) matcher.toMatchResult() else null }.toList()
    }

    private val koreanDays = mapOf(
        "월" to DayType.MON, "화" to DayType.TUE, "수" to DayType.WED, "목" to DayType.THU,
        "금" to DayType.FRI, "토" to DayType.SAT, "일" to DayType.SUN,
    )

    private val englishDays = mapOf(
        "monday" to DayType.MON, "mon" to DayType.MON,
        "tuesday" to DayType.TUE, "tues" to DayType.TUE, "tue" to DayType.TUE,
        "wednesday" to DayType.WED, "weds" to DayType.WED, "wed" to DayType.WED,
        "thursday" to DayType.THU, "thurs" to DayType.THU, "thur" to DayType.THU, "thu" to DayType.THU,
        "friday" to DayType.FRI, "fri" to DayType.FRI,
        "saturday" to DayType.SAT, "sat" to DayType.SAT,
        "sunday" to DayType.SUN, "sun" to DayType.SUN,
    )

    private val DayType.next: DayType get() = week[(week.indexOf(this) + 1) % week.size]
}
