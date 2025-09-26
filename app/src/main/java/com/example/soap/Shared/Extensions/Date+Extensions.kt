package com.example.soap.Shared.Extensions

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.timeAgoDisplay(): String {
    val nowCalendar = Calendar.getInstance()
    val thenCalendar = Calendar.getInstance()
    thenCalendar.time = this

//    val years = nowCalendar.get(Calendar.YEAR) - thenCalendar.get(Calendar.YEAR)
    val year = thenCalendar.get(Calendar.YEAR)
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
    if (days >= 7) return "$year/$month/$day"
    if (days > 0) return "$days day${if (days > 1) "s" else ""} ago"
    if (hours > 0) return "$hours hour${if (hours > 1) "s" else ""} ago"
    if (minutes > 0) return "${minutes} min ago "

    return "just now"
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


fun Date.relativeTimeString(): String {
    val time = localizedTime(this)

    return when {
        isDateInToday(this) -> "Today at $time"
        isDateInTomorrow(this) -> "Tomorrow at $time"
        else -> {
            weekdayNameIfWithinAWeek(this)?.let { "$it at $time" } ?: run {
                val formatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a", Locale.getDefault())
                val localDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
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

fun Date.formattedString(): String{
    val formatter = SimpleDateFormat("yyyy MMMM d EEE a h:mm", Locale.getDefault())
    return formatter.format(this)
}

fun Date.formattedTime(): String{
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(this)
}

fun Date.formattedDate(): String {
    val formatter = SimpleDateFormat("MMMM d, EEE", Locale.getDefault())
    return formatter.format(this)
}

fun Date.weekdaySymbol(): String{
    val weekdaySymbols = SimpleDateFormat("EEEE", Locale.getDefault())
    return weekdaySymbols.format(this)
}