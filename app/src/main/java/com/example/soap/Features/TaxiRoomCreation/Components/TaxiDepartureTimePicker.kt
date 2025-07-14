package com.example.soap.Features.TaxiRoomCreation.Components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaxiDepartureTimePicker(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(departureTime) {
        calendar.time = departureTime
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("MM/dd", Locale.getDefault())
    }
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Departure Time", style = MaterialTheme.typography.titleMedium)

            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.soapColors.gray0Border)
                        .padding(4.dp)
                        .clickable {
                            showDatePicker = true
                            showTimePicker = false
                        }
                ){
                    Text(dateFormatter.format(calendar.time))
                }

                Spacer(modifier = Modifier.padding(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.soapColors.gray0Border)
                        .padding(4.dp)
                        .clickable {
                            showTimePicker = true
                            showDatePicker = false
                        }) {
                    Text(timeFormatter.format(calendar.time))
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    onDepartureTimeChange(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
                val max = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 13) }
                datePicker.maxDate = max.timeInMillis
                show()
            }
        }

        if (showTimePicker) {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, (minute + 9) / 10 * 10 % 60)
                    calendar.set(Calendar.SECOND, 0)
                    onDepartureTimeChange(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }
}


@Composable
@Preview
private fun Preview(){
    SoapTheme {
        var time by remember { mutableStateOf(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))) }

        TaxiDepartureTimePicker(
            departureTime = time,
            onDepartureTimeChange = { time = it }
        )
    }
}