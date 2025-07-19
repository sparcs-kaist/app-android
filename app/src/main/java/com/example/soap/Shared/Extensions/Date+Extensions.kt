package com.example.soap.Shared.Extensions

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

fun Date.timeAgoDisplay(): String {
    val nowCalendar = Calendar.getInstance()
    val thenCalendar = Calendar.getInstance()
    thenCalendar.time = this

    val years = nowCalendar.get(Calendar.YEAR) - thenCalendar.get(Calendar.YEAR)
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

    if (years > 0) return "$year/$month/$day"
    if (days >= 7) return "$month/$day"
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

//relativeTimeString
//formattedTime
//weekdayNameIfWithinAWeek
