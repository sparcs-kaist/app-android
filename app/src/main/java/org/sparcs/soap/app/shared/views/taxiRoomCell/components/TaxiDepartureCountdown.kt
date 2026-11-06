package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.Date

/** How far ahead a ride's departure shows as a countdown rather than a date. */
const val TAXI_COUNTDOWN_WINDOW_MILLIS = 12 * DateUtils.HOUR_IN_MILLIS

/** The current time, updated on each minute, so countdowns and "Today" stay current on screen. */
@Composable
fun rememberCurrentMinute(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        delay(DateUtils.MINUTE_IN_MILLIS - value % DateUtils.MINUTE_IN_MILLIS)
        value = System.currentTimeMillis()
    }
}

/**
 * How long until [departAt] from [now], abbreviated ("In 25 min.", "In 3 hr."), or null once it
 * has left or while it is more than [TAXI_COUNTDOWN_WINDOW_MILLIS] away.
 */
fun taxiCountdown(departAt: Date, now: Long): String? {
    val untilDeparture = departAt.time - now
    if (untilDeparture <= 0 || untilDeparture >= TAXI_COUNTDOWN_WINDOW_MILLIS) return null
    return DateUtils.getRelativeTimeSpanString(
        departAt.time, now,
        if (untilDeparture < DateUtils.HOUR_IN_MILLIS) DateUtils.MINUTE_IN_MILLIS else DateUtils.HOUR_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}

/** Whether [departAt] is within the hour, when a countdown turns a warm colour. */
fun isDepartingSoon(departAt: Date, now: Long): Boolean =
    departAt.time - now in 1 until DateUtils.HOUR_IN_MILLIS
