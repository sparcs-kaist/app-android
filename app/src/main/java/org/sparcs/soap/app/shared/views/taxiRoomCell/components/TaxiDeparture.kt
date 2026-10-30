package org.sparcs.soap.app.shared.views.taxiRoomCell.components

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.extensions.isDateInToday
import org.sparcs.soap.app.shared.extensions.isDateInTomorrow
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.creditIncompleteColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val COUNTDOWN_WINDOW_MILLIS = 12 * DateUtils.HOUR_IN_MILLIS
private const val CLOCK_TICK_MILLIS = 30_000L

@Composable
fun rememberTaxiClock(): Long {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(CLOCK_TICK_MILLIS - value % CLOCK_TICK_MILLIS)
            value = System.currentTimeMillis()
        }
    }
    return now
}

fun Date.isWithinCountdown(now: Long): Boolean = time - now in 1 until COUNTDOWN_WINDOW_MILLIS

fun Date.isDepartingSoon(now: Long): Boolean = time - now in 1 until DateUtils.HOUR_IN_MILLIS

fun Date.departureCountdown(now: Long): String =
    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()

fun Date.taxiMonthDay(): String {
    val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMdEEE")
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}

@Composable
fun Date.taxiTime(): String = DateFormat.getTimeFormat(LocalContext.current).format(this)

@Composable
fun Date.taxiDayString(now: Long): String {
    val monthDay = taxiMonthDay()
    if (!isDateInToday(this) && !isDateInTomorrow(this)) return monthDay
    val relativeDay = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.DAY_IN_MILLIS).toString()
    return stringResource(R.string.taxi_departure_day, relativeDay, monthDay)
}

@Composable
fun TaxiDepartureDetail(departAt: Date, isDeparted: Boolean, now: Long, modifier: Modifier = Modifier) {
    val showsCountdown = !isDeparted && departAt.isWithinCountdown(now)
    val color: Color = when {
        showsCountdown && departAt.isDepartingSoon(now) -> creditIncompleteColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = if (showsCountdown) departAt.departureCountdown(now) else departAt.taxiMonthDay(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (showsCountdown) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    Theme {
        val now = System.currentTimeMillis()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            TaxiDepartureDetail(Date(now + 25 * 60_000), isDeparted = false, now = now)
            TaxiDepartureDetail(Date(now + 5 * DateUtils.HOUR_IN_MILLIS), isDeparted = false, now = now)
            TaxiDepartureDetail(Date(now + 3 * DateUtils.DAY_IN_MILLIS), isDeparted = false, now = now)
        }
    }
}
