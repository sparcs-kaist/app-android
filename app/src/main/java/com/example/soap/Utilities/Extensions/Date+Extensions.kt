package com.example.soap.Utilities.Extensions

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
    if (minutes > 0) return "${minutes}min ago "

    return "just now"
}