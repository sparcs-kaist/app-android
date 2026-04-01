package org.sparcs.soap.App.Shared.Extensions

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import org.sparcs.soap.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun Date.timeAgoDisplay(): String {
    val nowCalendar = Calendar.getInstance()
    val thenCalendar = Calendar.getInstance()
    thenCalendar.time = this

    val years = nowCalendar.get(Calendar.YEAR) - thenCalendar.get(Calendar.YEAR)
    val year = thenCalendar.get(Calendar.YEAR)
    val months = nowCalendar.get(Calendar.MONTH) - thenCalendar.get(Calendar.MONTH)
    val month = thenCalendar.get(Calendar.MONTH) + 1
    var days = nowCalendar.get(Calendar.DAY_OF_MONTH) - thenCalendar.get(Calendar.DAY_OF_MONTH)
    val day = thenCalendar.get(Calendar.DAY_OF_MONTH)
    var hours = nowCalendar.get(Calendar.HOUR_OF_DAY) - thenCalendar.get(Calendar.HOUR_OF_DAY)
    var minutes = nowCalendar.get(Calendar.MINUTE) - thenCalendar.get(Calendar.MINUTE)

    if (minutes < 0) {
        minutes += 60
        hours--
    }
    if (hours < 0) {
        hours += 24
        days--
    }
    if (days >= 7 || months > 0 || years > 0) return "$year. $month. $day"
    if (days > 0) return pluralStringResource(
        id = R.plurals.time_days_ago,
        count = days,
        days
    )
    if (hours > 0) return pluralStringResource(
        id = R.plurals.time_hours_ago,
        count = hours,
        hours
    )
    if (minutes > 0) return stringResource(R.string.minutes_ago, minutes)

    return stringResource(R.string.just_now)
}

fun Date.toISO8601(): String {
    val instant = this.toInstant()
    val formatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .withZone(ZoneOffset.UTC)

    return formatter.format(instant)
}

fun Date.toLocalDate(): LocalDate =
    toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDate.toDate(): Date =
    Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.toDate(): Date =
    Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

fun Date.ceilToNextTenMinutes(): Date {
    val cal = Calendar.getInstance().apply { time = this@ceilToNextTenMinutes }
    val minute = cal.get(Calendar.MINUTE)
    cal.set(Calendar.MINUTE, ((minute + 9) / 10) * 10)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun Calendar.isDateInSameDay(date1: Date, date2: Date): Boolean {
    val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate1 == localDate2
}

@Composable
fun Date.relativeTimeString(): String {
    val time = localizedTime(this)

    return when {
        isDateInToday(this) ->
            stringResource(R.string.today_at, time)

        isDateInTomorrow(this) ->
            stringResource(R.string.tomorrow_at, time)

        else -> {
            val weekday = weekdayNameIfWithinAWeek(this)

            if (weekday != null) {
                stringResource(R.string.weekday_at, weekday, time)
            } else {
                val locale = Locale.getDefault()
                val pattern = if (locale.language == "ko") {
                    "M월 d일 a h:mm"
                } else {
                    "MMM d 'at' h:mm a"
                }

                val formatter = DateTimeFormatter.ofPattern(pattern, locale)

                val localDateTime = LocalDateTime.ofInstant(
                    this.toInstant(),
                    ZoneId.systemDefault()
                )
                formatter.format(localDateTime)
            }
        }
    }
}


fun isDateInToday(date: Date): Boolean {
    val today = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }
    return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

fun isDateInTomorrow(date: Date): Boolean {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
    val target = Calendar.getInstance().apply { time = date }
    return tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

fun localizedTime(date: Date): String {
    val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
    return formatter.format(date)
}

fun weekdayNameIfWithinAWeek(date: Date): String? {
    val calendar = Calendar.getInstance()
    val now = calendar.time

    val startOfToday = calendar.apply {
        time = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val targetStart = calendar.apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val daysDiff = ((targetStart - startOfToday) / (1000 * 60 * 60 * 24)).toInt()

    return if (daysDiff in 1..6) {
        val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
        formatter.format(date)
    } else {
        null
    }
}

fun Date.formattedString(): String {
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yMMMMdEEEjm")
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

fun Date.formattedTime(): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(this)
}

fun Date.formattedDate(): String {
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMMdEEE")
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(this)
}

fun Date.weekdaySymbol(): String {
    val weekdaySymbols = SimpleDateFormat("EEEE", Locale.getDefault())
    return weekdaySymbols.format(this)
}

@Composable
fun DayOfWeek.toShortString(): String {
    return when (this) {
        DayOfWeek.MONDAY -> stringResource(R.string.mon)
        DayOfWeek.TUESDAY -> stringResource(R.string.tue)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.wed)
        DayOfWeek.THURSDAY -> stringResource(R.string.thu)
        DayOfWeek.FRIDAY -> stringResource(R.string.fri)
        DayOfWeek.SATURDAY -> stringResource(R.string.sat)
        DayOfWeek.SUNDAY -> stringResource(R.string.sun)
    }
}
