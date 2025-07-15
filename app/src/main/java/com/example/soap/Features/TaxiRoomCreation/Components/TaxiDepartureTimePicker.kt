package com.example.soap.Features.TaxiRoomCreation.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun TaxiDepartureTimePicker(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }

    LaunchedEffect(departureTime) {
        calendar.time = departureTime
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

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
                        .clickable {
                            showDatePicker = !showDatePicker
                            showTimePicker = false
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        dateFormatter.format(calendar.time),
                        color = if (showDatePicker) MaterialTheme.soapColors.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.soapColors.gray0Border)
                        .clickable {
                            showTimePicker = !showTimePicker
                            showDatePicker = false
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        timeFormatter.format(
                            calendar.apply { set(Calendar.MINUTE, (get(Calendar.MINUTE) / 10) * 10); set(Calendar.SECOND, 0) }.time
                        ),
                        color = if (showTimePicker) MaterialTheme.soapColors.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showDatePicker) {

        }

        if (showTimePicker) {
            CircularTimePicker(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE)
            ) { hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                onDepartureTimeChange(calendar.time)
            }
        }
    }
}



@Composable
fun CircularTimePicker(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val hours = (0..23).toList()
    val minutes = (0..59 step 10).toList()

    val displayHours = listOf("") + hours.map { it.toString().padStart(2, '0') } + listOf("")
    val displayMinutes = listOf("") + minutes.map { it.toString().padStart(2, '0') } + listOf("")

    val itemHeight = 30.dp
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hour + 2)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = minutes.indexOfFirst { it >= minute } + 2)

    LaunchedEffect(hourState.firstVisibleItemIndex, minuteState.firstVisibleItemIndex) {
        val selectedHour = (hourState.firstVisibleItemIndex).coerceIn(0, hours.lastIndex)
        val selectedMinute = (minuteState.firstVisibleItemIndex).coerceIn(0, minutes.lastIndex)
        onTimeSelected(hours[selectedHour], minutes[selectedMinute])
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * 5),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PickerWheel(
            items = displayHours,
            listState = hourState,
            itemHeight = itemHeight,
            text = "시"
        )

        PickerWheel(
            items = displayMinutes,
            listState = minuteState,
            itemHeight = itemHeight,
            text = "분"
        )
    }
}

@Composable
fun PickerWheel(
    items: List<String>,
    listState: LazyListState,
    itemHeight: Dp,
    text: String
) {
    val coroutineScope = rememberCoroutineScope()

    Row {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(itemHeight * 5)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(vertical = itemHeight),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            ) {
                itemsIndexed(items) { index, item ->
                    val centerIndex = listState.firstVisibleItemIndex +1
                    val distanceFromCenter = abs(index - centerIndex)
                    val alpha = when (distanceFromCenter) {
                        0 -> 1f
                        1 -> 0.5f
                        2 -> 0.2f
                        else -> 0.1f
                    }

                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .alpha(alpha)
                            .clickable {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.soapColors.grayBB.copy(alpha = 0.3f))
            )
        }

        Spacer(Modifier.padding(4.dp))

        Box(
            modifier = Modifier.height(itemHeight * 5)
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}



@Composable
@Preview
private fun Preview() {
    SoapTheme {
        Box(Modifier.background(MaterialTheme.soapColors.surface)){
            var time by remember {
                mutableStateOf(
                    Date.from(
                        Instant.now().plus(1, ChronoUnit.DAYS)
                    )
                )
            }
            TaxiDepartureTimePicker(
                departureTime = time,
                onDepartureTimeChange = { time = it }
            )
        }
    }
}
