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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.RemoveCircleOutline
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
import androidx.compose.runtime.saveable.listSaver
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.ActivityConflictException
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.ActivityRefreshRequiredException
import org.sparcs.soap.app.domain.models.otl.ActivityTime
import org.sparcs.soap.app.domain.models.otl.ParsedActivity
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.domain.models.otl.withActivityTimes
import org.sparcs.soap.app.features.taxiRoomCreation.components.PickerWheel
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.glassBorder
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.theme.ui.Theme
import org.sparcs.soap.app.theme.ui.grayBB

@Composable
internal fun activityTime(minutes: Int): String {
    if (minutes == 1440) return stringResource(R.string.activity_midnight)
    return LocalTime.of(minutes / 60, minutes % 60)
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

@Composable
internal fun activityTimeRange(time: ActivityTime): String {
    val day = DayType.fromValue(time.day)?.let { stringResource(it.stringValue) }.orEmpty()
    return "$day ${activityTime(time.begin)} – ${activityTime(time.end)}"
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
    quickFill: ActivityQuickFillState = ActivityQuickFillState(),
    onQuickFill: (String, (ParsedActivity) -> Unit) -> Unit = { _, _ -> },
    onQuickFillTextChange: () -> Unit = {},
) {
    val initial = remember {
        activity?.draft() ?: ActivityDraft.initial(
            timetable,
            (LocalDate.now().dayOfWeek.value - 1).let { if (it < 5) it else 0 })
    }
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var location by rememberSaveable { mutableStateOf(initial.location) }
    var times by rememberSaveable(stateSaver = activityTimesSaver) { mutableStateOf(initial.times) }
    var adjustingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    var needsRefresh by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val draft = ActivityDraft(title, location, times)
    val conflict = draft.conflict(timetable, activity?.id) || draft.hasOverlappingTimes
    val applyParsed: (ParsedActivity) -> Unit = { parsed ->
        val filled = parsed.applied(draft)
        title = filled.title
        location = filled.location
        times = filled.times
    }

    BackHandler(enabled = saving) { }

    val adjusted = adjustingIndex?.takeIf { it in times.indices }
    if (adjusted != null) {
        ActivityTimetableCreationView(
            timetable.withActivityTimes(activity?.id, title, times.filterIndexed { i, _ -> i != adjusted }),
            draft.copy(times = listOf(times[adjusted])),
            activity?.id,
            onChange = { changed -> times = times.toMutableList().also { it[adjusted] = changed.times.first() } },
            onBack = { adjustingIndex = null })
    } else {
        Scaffold(
            modifier = Modifier.analyticsScreen("ActivityCreation"),
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

                if (activity == null && quickFill.isAvailable) {
                    ActivityQuickFillCard(
                        state = quickFill,
                        enabled = !saving && !needsRefresh,
                        onTextChange = onQuickFillTextChange,
                        onFill = { description ->
                            focus.clearFocus()
                            onQuickFill(description, applyParsed)
                        },
                    )
                }

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

                times.forEachIndexed { index, time ->
                    ActivityTimeCard(
                        time = time,
                        enabled = !saving && !needsRefresh,
                        conflict = draft.conflict(timetable, time, activity?.id) || times.withIndex().any { (other, it) -> other != index && it.overlaps(time) },
                        onChange = { times = times.toMutableList().also { list -> list[index] = it } },
                        onAdjust = { focus.clearFocus(); adjustingIndex = index },
                        onRemove = if (times.size > 1) { { times = times.filterIndexed { i, _ -> i != index } } } else null,
                    )
                }

                TextButton(
                    onClick = {
                        focus.clearFocus()
                        times = times + ActivityDraft.freeTime(timetable, times.last().day, times)
                    },
                    enabled = !saving && !needsRefresh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.activity_add_time))
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
private fun ActivityTimeCard(
    time: ActivityTime,
    enabled: Boolean,
    conflict: Boolean,
    onChange: (ActivityTime) -> Unit,
    onAdjust: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val focus = LocalFocusManager.current
    var timeField by rememberSaveable { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.5f
                                    )
                                )
                                .clickable(enabled = enabled) {
                                    focus.clearFocus(); expandedDay = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(DayType.fromValue(time.day)!!.fullStringValue),
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
                                        onChange(time.move(time.begin, option.value))
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
                            .clickable(enabled = enabled) {
                                focus.clearFocus()
                                timeField = if (timeField == 1) 0 else 1
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            activityTime(time.begin),
                            color = if (timeField == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (timeField == 1) {
                    var selectedHour by remember(time.begin) { mutableIntStateOf(time.begin / 60) }
                    var selectedMinute by remember(time.begin) { mutableIntStateOf(time.begin % 60) }
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
                                onChange(time.copy(begin = newBegin, end = if (newBegin >= time.end) (newBegin + 15).coerceAtMost(1440) else time.end))
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
                            .clickable(enabled = enabled) {
                                focus.clearFocus()
                                timeField = if (timeField == 2) 0 else 2
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            activityTime(time.end),
                            color = if (timeField == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (timeField == 2) {
                    val currentEnd = time.end % 1440
                    var selectedHour by remember(time.end) { mutableIntStateOf(currentEnd / 60) }
                    var selectedMinute by remember(time.end) { mutableIntStateOf(currentEnd % 60) }
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
                                onChange(time.copy(begin = if (newEnd <= time.begin) (newEnd - 15).coerceAtLeast(0) else time.begin, end = newEnd))
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onAdjust,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.activity_adjust))
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove, enabled = enabled) {
                    Icon(
                        Icons.Rounded.RemoveCircleOutline,
                        stringResource(R.string.activity_remove_time),
                        tint = if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.grayBB
                    )
                }
            }
        }
        if (conflict) {
            Text(
                stringResource(R.string.activity_conflict),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private val activityTimesSaver = listSaver<List<ActivityTime>, Int>(
    save = { times -> times.flatMap { listOf(it.day, it.begin, it.end) } },
    restore = { values -> values.chunked(3).map { ActivityTime(it[0], it[1], it[2]) } },
)

@Composable
private fun ActivityQuickFillCard(
    state: ActivityQuickFillState,
    enabled: Boolean,
    onTextChange: () -> Unit,
    onFill: (String) -> Unit,
) {
    var description by rememberSaveable { mutableStateOf("") }
    val canFill = enabled && description.isNotBlank() && !state.isParsing
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.activity_quick_fill),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.grayBB,
            modifier = Modifier.padding(start = 8.dp)
        )
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.glassBorder(shape = RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = { description = it; onTextChange() },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    enabled = enabled && !state.isParsing,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canFill) onFill(description) }),
                    decorationBox = { innerTextField ->
                        if (description.isEmpty()) {
                            Text(
                                text = stringResource(R.string.activity_quick_fill_placeholder),
                                color = MaterialTheme.colorScheme.grayBB,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                )
                if (state.isParsing) {
                    CircularProgressIndicator(
                        Modifier
                            .padding(12.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { onFill(description) }, enabled = canFill) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            stringResource(R.string.activity_quick_fill_action),
                            tint = if (canFill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.grayBB
                        )
                    }
                }
            }
        }
        Text(
            stringResource(state.errorRes ?: R.string.activity_quick_fill_footer),
            style = MaterialTheme.typography.bodySmall,
            color = if (state.errorRes != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.grayBB,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
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

@Composable
@Preview
private fun QuickFillPreview() {
    Theme {
        ActivityCreationView(
            timetable = Timetable.mock(),
            timetableName = "Test1",
            onClose = {},
            onSave = {},
            onRefresh = {},
            quickFill = ActivityQuickFillState(isAvailable = true),
        )
    }
}

@Composable
@Preview
private fun QuickFillCardPreview() {
    Theme {
        ActivityQuickFillCard(
            state = ActivityQuickFillState(isAvailable = true, errorRes = R.string.activity_quick_fill_nothing_found),
            enabled = true,
            onTextChange = {},
            onFill = {},
        )
    }
}

@Composable
@Preview
private fun SeveralTimesPreview() {
    Theme {
        ActivityCreationView(
            timetable = Timetable.mock(),
            timetableName = "Test1",
            activity = TimetableActivity(
                1, "Study group", "Library",
                listOf(ActivityTime(1, 1140, 1260), ActivityTime(3, 1140, 1260)),
            ),
            onClose = {},
            onSave = {},
            onRefresh = {}
        )
    }
}

@Composable
@Preview
private fun TimeCardPreview() {
    Theme {
        ActivityTimeCard(
            time = ActivityTime(1, 1140, 1260),
            enabled = true,
            conflict = true,
            onChange = {},
            onAdjust = {},
            onRemove = {},
        )
    }
}
