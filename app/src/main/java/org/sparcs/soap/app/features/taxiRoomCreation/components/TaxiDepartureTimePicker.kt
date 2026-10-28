package org.sparcs.soap.app.features.taxiRoomCreation.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filterNotNull
import org.sparcs.soap.R
import org.sparcs.soap.app.shared.views.contentViews.PickerField
import org.sparcs.soap.app.shared.views.contentViews.TimeWheel
import org.sparcs.soap.app.theme.ui.Theme
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Rooms can be scheduled up to this many days ahead, today included. */
private const val BOOKABLE_DAYS = 14

/** Departure times snap to this many minutes. */
private const val MINUTE_STEP = 10

/** Which picker is open below the fields, if any. */
private enum class OpenPicker { Date, Time }

/**
 * The departure date and time as read-only fields; tapping one opens the Material date picker or
 * time wheels right below it. Dates run from today for two weeks, and times snap to ten minutes.
 */
@Composable
fun TaxiDepartureTimePicker(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit,
) {
    var openPicker by rememberSaveable { mutableStateOf<OpenPicker?>(null) }
    val dateText = remember(departureTime) { SimpleDateFormat("MM/dd", Locale.getDefault()).format(departureTime) }
    val context = LocalContext.current
    // Follows the system's 12- or 24-hour setting, like "3:20 PM" or "15:20".
    val timeText = remember(departureTime) { DateFormat.getTimeFormat(context).format(departureTime) }
    val toggle = { picker: OpenPicker -> openPicker = if (openPicker == picker) null else picker }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.departure_time),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.semantics { heading() }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PickerField(
                label = stringResource(R.string.date),
                value = dateText,
                icon = Icons.Outlined.CalendarToday,
                isOpen = openPicker == OpenPicker.Date,
                modifier = Modifier.weight(1f)
            ) { toggle(OpenPicker.Date) }
            PickerField(
                label = stringResource(R.string.time),
                value = timeText,
                icon = Icons.Outlined.Schedule,
                isOpen = openPicker == OpenPicker.Time,
                modifier = Modifier.weight(1f)
            ) { toggle(OpenPicker.Time) }
        }

        AnimatedVisibility(openPicker == OpenPicker.Date) {
            DepartureDatePicker(departureTime, onDepartureTimeChange)
        }
        AnimatedVisibility(openPicker == OpenPicker.Time) {
            DepartureTimeWheel(departureTime, onDepartureTimeChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DepartureDatePicker(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    val today = remember { LocalDate.now(zone) }
    val lastDay = remember(today) { today.plusDays(BOOKABLE_DAYS - 1L) }
    // The date picker works in UTC midnights.
    fun LocalDate.utcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = departureTime.toInstant().atZone(zone).toLocalDate().utcMillis(),
        yearRange = today.year..lastDay.year,
        selectableDates = remember(today, lastDay) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val day = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return !day.isBefore(today) && !day.isAfter(lastDay)
                }

                override fun isSelectableYear(year: Int) = year in today.year..lastDay.year
            }
        }
    )
    val currentTime by rememberUpdatedState(departureTime)
    val onChange by rememberUpdatedState(onDepartureTimeChange)
    LaunchedEffect(state) {
        snapshotFlow { state.selectedDateMillis }.filterNotNull().collect {
            val date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
            val calendar = Calendar.getInstance().apply { time = currentTime }
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
            if (calendar.time != currentTime) onChange(calendar.time)
        }
    }
    DatePicker(state = state, showModeToggle = false)
}

/** The departure time on [TimeWheel], in ten-minute steps. */
@Composable
private fun DepartureTimeWheel(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit,
) {
    val calendar = remember { Calendar.getInstance().apply { time = departureTime } }
    val currentTime by rememberUpdatedState(departureTime)
    TimeWheel(
        minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE),
        step = MINUTE_STEP,
    ) { minutes ->
        val updated = Calendar.getInstance().apply {
            time = currentTime
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        if (updated != currentTime) onDepartureTimeChange(updated)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Theme {
        Box(Modifier.padding(16.dp)) {
            TaxiDepartureTimePicker(departureTime = Date(), onDepartureTimeChange = {})
        }
    }
}
