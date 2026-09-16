package org.sparcs.soap.app.features.timetable.activity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityConflictException
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.ActivityRefreshRequiredException
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.features.taxiRoomCreation.components.PickerWheel
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun activityTime(minutes: Int): String {
    if (minutes == 1440) return stringResource(R.string.activity_midnight)
    return LocalTime.of(minutes / 60, minutes % 60)
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCreationView(
    timetable: Timetable,
    timetableName: String,
    activity: TimetableActivity? = null,
    onClose: () -> Unit,
    onSave: suspend (ActivityDraft) -> Unit,
    onRefresh: suspend () -> Unit,
) {
    val initial = remember {
        activity?.draft() ?: ActivityDraft.initial(
            timetable,
            (LocalDate.now().dayOfWeek.value - 1).let { if (it < 5) it else 0 })
    }
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var location by rememberSaveable { mutableStateOf(initial.location) }
    var day by rememberSaveable { mutableIntStateOf(initial.day) }
    var begin by rememberSaveable { mutableIntStateOf(initial.begin) }
    var end by rememberSaveable { mutableIntStateOf(initial.end) }
    var adjusting by rememberSaveable { mutableStateOf(false) }
    var timeField by rememberSaveable { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var needsRefresh by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val draft = ActivityDraft(title, location, day, begin, end)
    val conflict = draft.conflict(timetable, activity?.id)
    val update: (ActivityDraft) -> Unit = { day = it.day; begin = it.begin; end = it.end }

    BackHandler(enabled = saving) { }

    if (adjusting) {
        ActivityTimetableCreationView(
            timetable,
            draft,
            activity?.id,
            onChange = update,
            onBack = { adjusting = false })
    } else {
        Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(if (activity == null) R.string.activity_new else R.string.activity_edit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onClose,
                                enabled = !saving
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    stringResource(R.string.activity_close),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        },
                        actions = {
                            if (saving) CircularProgressIndicator(
                                Modifier
                                    .padding(16.dp)
                                    .size(24.dp),
                                strokeWidth = 2.dp
                            )
                            else TextButton(
                                enabled = needsRefresh || (draft.isValid && !conflict),
                                onClick = {
                                    focus.clearFocus()
                                    scope.launch {
                                        saving = true
                                        try {
                                            if (needsRefresh) onRefresh() else onSave(draft)
                                            onClose()
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            error = when (e) {
                                                is ActivityConflictException -> R.string.activity_conflict
                                                is ActivityRefreshRequiredException -> {
                                                    needsRefresh = true; R.string.activity_saved_refresh
                                                }

                                                else -> R.string.activity_save_error
                                            }
                                        } finally {
                                            saving = false
                                        }
                                    }
                                }) {
                                Text(
                                    stringResource(if (needsRefresh) R.string.activity_refresh else R.string.activity_save),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = if (needsRefresh || (draft.isValid && !conflict)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        timetableName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.glassBorder(shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                enabled = !saving && !needsRefresh,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                decorationBox = { innerTextField ->
                                    if (title.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.activity_title),
                                            color = MaterialTheme.colorScheme.grayBB,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                            BasicTextField(
                                value = location,
                                onValueChange = { location = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                enabled = !saving && !needsRefresh,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                                decorationBox = { innerTextField ->
                                    if (location.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.activity_location),
                                            color = MaterialTheme.colorScheme.grayBB,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .glassBorder(shape = RoundedCornerShape(16.dp))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            var expandedDay by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.activity_day),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable(enabled = !saving && !needsRefresh) {
                                                focus.clearFocus(); expandedDay = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stringResource(DayType.fromValue(day)!!.fullStringValue),
                                            color = if (expandedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Icon(
                                            Icons.Rounded.ExpandMore,
                                            null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (expandedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expandedDay,
                                        onDismissRequest = { expandedDay = false },
                                        containerColor = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        DayType.entries.sortedBy { it.value }.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(stringResource(option.fullStringValue)) },
                                                onClick = {
                                                    day = option.value
                                                    expandedDay = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(Modifier.padding(vertical = 16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.activity_starts),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable(enabled = !saving && !needsRefresh) {
                                            focus.clearFocus()
                                            timeField = if (timeField == 1) 0 else 1
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        activityTime(begin),
                                        color = if (timeField == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            if (timeField == 1) {
                                var selectedHour by remember(begin) { mutableIntStateOf(begin / 60) }
                                var selectedMinute by remember(begin) { mutableIntStateOf(begin % 60) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WheelTimePicker(
                                        hour = selectedHour,
                                        minute = selectedMinute,
                                        onTimeSelected = { h, m ->
                                            selectedHour = h
                                            selectedMinute = m
                                            val newBegin = h * 60 + m
                                            if (newBegin >= end) {
                                                end = (newBegin + 15).coerceAtMost(1440)
                                            }
                                            begin = newBegin
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(Modifier.padding(vertical = 16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.activity_ends),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable(enabled = !saving && !needsRefresh) {
                                            focus.clearFocus()
                                            timeField = if (timeField == 2) 0 else 2
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        activityTime(end),
                                        color = if (timeField == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            if (timeField == 2) {
                                val currentEnd = end % 1440
                                var selectedHour by remember(end) { mutableIntStateOf(currentEnd / 60) }
                                var selectedMinute by remember(end) { mutableIntStateOf(currentEnd % 60) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WheelTimePicker(
                                        hour = selectedHour,
                                        minute = selectedMinute,
                                        onTimeSelected = { h, m ->
                                            selectedHour = h
                                            selectedMinute = m
                                            val newEnd = if (h * 60 + m == 0) 1440 else h * 60 + m
                                            if (newEnd <= begin) {
                                                begin = (newEnd - 15).coerceAtLeast(0)
                                            }
                                            end = newEnd
                                        }
                                    )
                                }
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = { focus.clearFocus(); adjusting = true },
                        enabled = !saving && !needsRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.activity_adjust))
                    }
                    ActivityConflictNotice(conflict)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

    if (error != 0) AlertDialog(
        onDismissRequest = { error = 0 },
        text = { Text(stringResource(error)) },
        containerColor = MaterialTheme.colorScheme.background,
        confirmButton = {
            TextButton(onClick = {
                error = 0
            }) { Text(stringResource(R.string.ok)) }
        })
}

@Composable
private fun WheelTimePicker(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59 step 15).toList() }

    val itemHeight = 35.dp
    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = hour)
    val minuteState = rememberLazyListState(
        initialFirstVisibleItemIndex = (minute / 15).coerceIn(0, minutes.lastIndex)
    )

    LaunchedEffect(hourState.isScrollInProgress, minuteState.isScrollInProgress) {
        if (!hourState.isScrollInProgress && !minuteState.isScrollInProgress) {
            val selectedHour = hourState.firstVisibleItemIndex.coerceIn(0, hours.lastIndex)
            val selectedMin = minuteState.firstVisibleItemIndex.coerceIn(0, minutes.lastIndex)
            onTimeSelected(hours[selectedHour], minutes[selectedMin])
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * 5),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PickerWheel(
            items = hours.map { it.toString().padStart(2, '0') },
            listState = hourState,
            itemHeight = itemHeight,
            text = stringResource(R.string.hour_label)
        )
        Spacer(Modifier.width(24.dp))
        PickerWheel(
            items = minutes.map { it.toString().padStart(2, '0') },
            listState = minuteState,
            itemHeight = itemHeight,
            text = stringResource(R.string.minute_label)
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Theme {
        ActivityCreationView(
            timetable = Timetable.mock(),
            timetableName = "Test1",
            activity = null,
            onClose = {},
            onSave = {},
            onRefresh = {}
        )
    }
}
