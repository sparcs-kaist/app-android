package org.sparcs.soap.app.shared.formatters

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Formatter
import java.util.Locale

private const val MINUTES_PER_DAY = 24 * 60

fun Context.formatTime(minutes: Int): String =
    timeFormatter(locale, DateFormat.is24HourFormat(this)).format(timeOfDay(minutes))

fun Context.formatTimeRange(begin: Int, end: Int): String {
    if (begin !in 0 until MINUTES_PER_DAY || end !in begin until MINUTES_PER_DAY) {
        return "${formatTime(begin)} – ${formatTime(end)}"
    }
    val hourFlag = if (DateFormat.is24HourFormat(this)) DateUtils.FORMAT_24HOUR else DateUtils.FORMAT_12HOUR
    return DateUtils.formatDateRange(
        this,
        Formatter(StringBuilder(), locale),
        begin * DateUtils.MINUTE_IN_MILLIS,
        end * DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_SHOW_TIME or hourFlag,
        "UTC"
    ).toString()
}

fun Context.formatHour(hour: Int): String {
    if (DateFormat.is24HourFormat(this)) return hour.toString()
    val hourOfHalfDay = hour.mod(12)
    val startsAtZero = DateFormat.getBestDateTimePattern(locale, "hm").contains('K')
    return if (hourOfHalfDay == 0 && !startsAtZero) "12" else hourOfHalfDay.toString()
}

@Composable
fun formattedTime(minutes: Int): String {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(is24Hour, locale) { timeFormatter(locale, is24Hour) }
    return formatter.format(timeOfDay(minutes))
}

private val Context.locale: Locale
    get() = resources.configuration.locales[0]

private fun timeFormatter(locale: Locale, is24Hour: Boolean): DateTimeFormatter {
    val skeleton = if (is24Hour) "HHmm" else "hmma"
    return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
}

private fun timeOfDay(minutes: Int): LocalTime =
    LocalTime.ofSecondOfDay(minutes.mod(MINUTES_PER_DAY) * 60L)
