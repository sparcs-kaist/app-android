package org.sparcs.soap.App.Features.TaxiRoomCreation.Components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Shared.Extensions.toDate
import org.sparcs.soap.App.Shared.Extensions.toLocalDate
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.darkGray
import org.sparcs.soap.App.theme.ui.grayBB
import org.sparcs.soap.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun TaxiDepartureTimePicker(
    departureTime: Date,
    onDepartureTimeChange: (Date) -> Unit,
) {
    val calendar = remember { Calendar.getInstance() }

    val initialDate = remember(departureTime) { departureTime.toLocalDate() }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val dateText = remember { mutableStateOf(dateFormatter.format(calendar.time)) }
    val timeText = remember {
        mutableStateOf(timeFormatter.format(calendar.apply {
            set(
                Calendar.MINUTE,
                (get(Calendar.MINUTE) / 10 + 1) * 10
            ); set(Calendar.SECOND, 0)
        }.time))
    }

    LaunchedEffect(departureTime) {
        calendar.time = departureTime
        dateText.value = dateFormatter.format(calendar.time)
        timeText.value = timeFormatter.format(calendar.time)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.departure_time),
                style = MaterialTheme.typography.titleMedium
            )

            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            showDatePicker = !showDatePicker
                            showTimePicker = false
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        dateText.value,
                        color = if (showDatePicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.padding(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            showTimePicker = !showTimePicker
                            showDatePicker = false
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        timeText.value,
                        color = if (showTimePicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (showDatePicker) {
            CustomDatePicker(
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                    calendar.set(Calendar.DATE, selectedDate.dayOfMonth)
                    calendar.set(Calendar.MONTH, selectedDate.monthValue - 1)
                    dateText.value = DateTimeFormatter.ofPattern("MM/dd").format(it)
                    onDepartureTimeChange(
                        calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            .toDate()
                    )
                }
            )
        }

        if (showTimePicker) {
            CircularTimePicker(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE)
            ) { hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                timeText.value = timeFormatter.format(calendar.time)
                onDepartureTimeChange(
                    calendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toDate()
                )
            }
        }
    }
}

@Composable
fun CircularTimePicker(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
) {
    val hours = (0..23).toList()
    val minutes = (0..59 step 10).toList()

    val displayHours = listOf("") + hours.map { it.toString().padStart(2, '0') } + listOf("")
    val displayMinutes = listOf("") + minutes.map { it.toString().padStart(2, '0') } + listOf("")

    val itemHeight = 30.dp
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hour)
    val minuteState =
        rememberLazyListState(initialFirstVisibleItemIndex = minutes.indexOfFirst { it >= minute })

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
            text = stringResource(R.string.hour_label)
        )

        PickerWheel(
            items = displayMinutes,
            listState = minuteState,
            itemHeight = itemHeight,
            text = stringResource(R.string.minute_label)
        )
    }
}

@Composable
fun PickerWheel(
    items: List<String>,
    listState: LazyListState,
    itemHeight: Dp,
    text: String,
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
                    val centerIndex = listState.firstVisibleItemIndex + 1
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
                                    listState.animateScrollToItem(index - 1)
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
                    .background(MaterialTheme.colorScheme.grayBB.copy(alpha = 0.3f))
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
fun CustomDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = LocalDate.now()
    val startDate = if (today.dayOfWeek == DayOfWeek.SUNDAY) {
        today
    } else {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    }
    val endDate = today.plusWeeks(2).with(DayOfWeek.SATURDAY)
    val days = (0L..ChronoUnit.DAYS.between(startDate, endDate)).map { startDate.plusDays(it) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentSize()
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(15.dp),
                    tint = MaterialTheme.colorScheme.grayBB
                )
                Text(
                    stringResource(
                        R.string.date_label,
                        selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.padding(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DayType.entries.forEach { dayType ->
                Text(
                    text = stringResource(dayType.stringValue),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (dayType) {
                        DayType.SUN -> Color.Red
                        DayType.SAT -> Color.Blue
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.padding(8.dp))

        days.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                week.forEach { day ->
                    val isDisabled = day < today || day > today.plusDays(13)
                    val isSelected = day == selectedDate
                    val dayType = DayType.fromValue(day.dayOfWeek.value % 7)

                    val bgColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isDisabled -> MaterialTheme.colorScheme.grayBB
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }

                    val textColor = when {
                        isDisabled -> MaterialTheme.colorScheme.darkGray
                        isSelected -> MaterialTheme.colorScheme.surface
                        dayType == DayType.SUN -> Color.Red
                        dayType == DayType.SAT -> Color.Blue
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    val dot = day == today

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable(enabled = !isDisabled) { onDateSelected(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = day.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                            if (dot) {
                                Spacer(modifier = Modifier.padding(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Theme {
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
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
