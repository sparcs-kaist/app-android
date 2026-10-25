package org.sparcs.soap.app.features.timetable.activity

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.theme.ui.Theme

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
    val tipStore = LocalTipStore.current
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
                        Text(stringResource(if (activity == null) R.string.activity_new else R.string.activity_edit))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onClose,
                            enabled = !saving
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                stringResource(R.string.activity_close)
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
                            Text(stringResource(if (needsRefresh) R.string.activity_refresh else R.string.activity_save))
                        }
                    }
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
                FormSectionHeader(timetableName)

                if (activity == null && quickFill.isAvailable) {
                    TipView(BuddyTip.QuickFill)
                    ActivityQuickFillField(
                        state = quickFill,
                        enabled = !saving && !needsRefresh,
                        onTextChange = onQuickFillTextChange,
                        onFill = { description ->
                            focus.clearFocus()
                            tipStore?.invalidate(BuddyTip.QuickFill)
                            onQuickFill(description, applyParsed)
                        },
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.activity_title)) },
                    enabled = !saving && !needsRefresh,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.activity_location)) },
                    enabled = !saving && !needsRefresh,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )

                times.forEachIndexed { index, time ->
                    ActivityTimeCard(
                        number = index + 1,
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
                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
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
        confirmButton = {
            TextButton(onClick = {
                error = 0
            }) { Text(stringResource(R.string.ok)) }
        })
}

/** A heading in the form, in the style of the settings section headers. */
@Composable
private fun FormSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { heading() }
    )
}

private enum class TimeField { Begin, End }

/** One meeting time: its day, start and end, a link to adjust it on the timetable, and delete. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTimeCard(
    number: Int,
    time: ActivityTime,
    enabled: Boolean,
    conflict: Boolean,
    onChange: (ActivityTime) -> Unit,
    onAdjust: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val focus = LocalFocusManager.current
    var editing by rememberSaveable { mutableStateOf<TimeField?>(null) }
    var expandedDay by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    stringResource(R.string.activity_time_number, number),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                if (onRemove != null) {
                    IconButton(onClick = onRemove, enabled = enabled) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.activity_remove_time))
                    }
                }
            }

            Column(
                modifier = Modifier.padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedDay,
                    onExpandedChange = { if (enabled) { focus.clearFocus(); expandedDay = it } },
                ) {
                    OutlinedTextField(
                        value = stringResource(DayType.fromValue(time.day)!!.fullStringValue),
                        onValueChange = {},
                        readOnly = true,
                        enabled = enabled,
                        singleLine = true,
                        label = { Text(stringResource(R.string.activity_day)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDay) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                        DayType.entries.sortedBy { it.value }.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.fullStringValue)) },
                                onClick = {
                                    onChange(time.move(time.begin, option.value))
                                    expandedDay = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeFieldButton(
                        label = stringResource(R.string.activity_starts),
                        value = activityTime(time.begin),
                        enabled = enabled,
                        isError = conflict,
                        modifier = Modifier.weight(1f)
                    ) { focus.clearFocus(); editing = TimeField.Begin }
                    TimeFieldButton(
                        label = stringResource(R.string.activity_ends),
                        value = activityTime(time.end),
                        enabled = enabled,
                        isError = conflict,
                        modifier = Modifier.weight(1f)
                    ) { focus.clearFocus(); editing = TimeField.End }
                }

                if (conflict) {
                    Text(
                        stringResource(R.string.activity_conflict),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                OutlinedButton(
                    onClick = onAdjust,
                    enabled = enabled,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.activity_adjust))
                }
            }
        }
    }

    when (editing) {
        TimeField.Begin -> ActivityTimePickerDialog(
            title = stringResource(R.string.activity_starts),
            minutes = time.begin,
            onDismiss = { editing = null },
        ) { newBegin ->
            onChange(time.copy(begin = newBegin, end = if (newBegin >= time.end) (newBegin + 15).coerceAtMost(1440) else time.end))
        }

        TimeField.End -> ActivityTimePickerDialog(
            title = stringResource(R.string.activity_ends),
            minutes = time.end % 1440,
            onDismiss = { editing = null },
        ) { picked ->
            // 00:00 as an end time means midnight at the end of the day.
            val newEnd = if (picked == 0) 1440 else picked
            onChange(time.copy(begin = if (newEnd <= time.begin) (newEnd - 15).coerceAtLeast(0) else time.begin, end = newEnd))
        }

        null -> Unit
    }
}

/** A read-only outlined field showing a time; tapping it opens the time picker. */
@Composable
private fun TimeFieldButton(
    label: String,
    value: String,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction, enabled) {
        interaction.interactions.collect { if (enabled && it is PressInteraction.Release) onClick() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
        interactionSource = interaction,
        modifier = modifier
    )
}

/**
 * The Material time picker in a dialog, with a button to switch between the clock and typing.
 * The chosen time snaps to the nearest 15 minutes, like the timetable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTimePickerDialog(
    title: String,
    minutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = minutes / 60,
        initialMinute = minutes % 60,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    var typing by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (typing) TimeInput(state) else TimePicker(state)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(ActivityDraft.snap((state.hour * 60 + state.minute).toFloat()) % 1440)
                onDismiss()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { typing = !typing }) {
                    Text(stringResource(if (typing) R.string.activity_clock else R.string.activity_keyboard))
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

private val activityTimesSaver = listSaver<List<ActivityTime>, Int>(
    save = { times -> times.flatMap { listOf(it.day, it.begin, it.end) } },
    restore = { values -> values.chunked(3).map { ActivityTime(it[0], it[1], it[2]) } },
)

/** Describe the activity in words; the on-device model fills in the fields below. */
@Composable
private fun ActivityQuickFillField(
    state: ActivityQuickFillState,
    enabled: Boolean,
    onTextChange: () -> Unit,
    onFill: (String) -> Unit,
) {
    var description by rememberSaveable { mutableStateOf("") }
    val canFill = enabled && description.isNotBlank() && !state.isParsing
    OutlinedTextField(
        value = description,
        onValueChange = { description = it; onTextChange() },
        label = { Text(stringResource(R.string.activity_quick_fill)) },
        placeholder = { Text(stringResource(R.string.activity_quick_fill_placeholder)) },
        supportingText = { Text(stringResource(state.errorRes ?: R.string.activity_quick_fill_footer)) },
        isError = state.errorRes != null,
        enabled = enabled && !state.isParsing,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (canFill) onFill(description) }),
        trailingIcon = {
            if (state.isParsing) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = { onFill(description) }, enabled = canFill) {
                    Icon(Icons.Outlined.AutoAwesome, stringResource(R.string.activity_quick_fill_action))
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
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
        ActivityQuickFillField(
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
            number = 1,
            time = ActivityTime(1, 1140, 1260),
            enabled = true,
            conflict = true,
            onChange = {},
            onAdjust = {},
            onRemove = {},
        )
    }
}
