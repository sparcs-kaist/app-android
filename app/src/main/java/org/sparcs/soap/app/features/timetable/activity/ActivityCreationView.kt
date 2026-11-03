package org.sparcs.soap.app.features.timetable.activity

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
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
import org.sparcs.soap.app.features.settings.components.SettingsFieldItem
import org.sparcs.soap.app.features.settings.components.SettingsGroup
import org.sparcs.soap.app.features.settings.components.SettingsNavigationItem
import org.sparcs.soap.app.features.settings.components.SettingsSectionHeader
import org.sparcs.soap.app.features.settings.components.settingsTextFieldColors
import org.sparcs.soap.app.shared.extensions.analyticsScreen
import org.sparcs.soap.app.shared.extensions.onlyBottom
import org.sparcs.soap.app.shared.extensions.withoutBottom
import org.sparcs.soap.app.shared.formatters.formattedTime
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.tips.BuddyTip
import org.sparcs.soap.app.shared.tips.LocalTipStore
import org.sparcs.soap.app.shared.tips.TipView
import org.sparcs.soap.app.shared.views.contentViews.ExpressiveMenu
import org.sparcs.soap.app.shared.views.contentViews.MenuGroup
import org.sparcs.soap.app.shared.views.contentViews.TimeWheel
import org.sparcs.soap.app.shared.views.contentViews.selectableMenuItemColors
import org.sparcs.soap.app.theme.ui.Theme

@Composable
internal fun activityTime(minutes: Int): String {
    val time = formattedTime(minutes)
    return if (minutes == 1440) stringResource(R.string.activity_midnight) else time
}

@Composable
internal fun activityTimeRange(time: ActivityTime): String {
    val day = DayType.fromValue(time.day)?.let { stringResource(it.stringValue) }.orEmpty()
    return "$day ${activityTime(time.begin)} – ${activityTime(time.end)}"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
                            enabled = !saving,
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                stringResource(R.string.activity_close)
                            )
                        }
                    },
                    actions = {
                        if (saving) LoadingIndicator(
                            Modifier
                                .padding(16.dp)
                                .size(24.dp)
                        )
                        else TextButton(
                            enabled = needsRefresh || (draft.isValid && !conflict),
                            shapes = ButtonDefaults.shapes(),
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
                    .padding(padding.withoutBottom())
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(padding.onlyBottom())
                    .padding(bottom = 32.dp)
            ) {
                // The form in the style of Settings: a header per section over one card of rows.
                SettingsSectionHeader(timetableName)

                if (activity == null && quickFill.isAvailable) {
                    TipView(BuddyTip.QuickFill, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
                    ActivityQuickFillField(
                        state = quickFill,
                        enabled = !saving && !needsRefresh,
                        onTextChange = onQuickFillTextChange,
                        onFill = { description ->
                            focus.clearFocus()
                            tipStore?.invalidate(BuddyTip.QuickFill)
                            onQuickFill(description, applyParsed)
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                SettingsGroup {
                    row {
                        SettingsFieldItem {
                            TextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text(stringResource(R.string.activity_title)) },
                                enabled = !saving && !needsRefresh,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                colors = settingsTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    row {
                        SettingsFieldItem {
                            TextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text(stringResource(R.string.activity_location)) },
                                enabled = !saving && !needsRefresh,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                                colors = settingsTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

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

                SettingsGroup(Modifier.padding(top = 16.dp)) {
                    row {
                        SettingsNavigationItem(
                            text = stringResource(R.string.activity_add_time),
                            onClick = {
                                focus.clearFocus()
                                times = times + ActivityDraft.freeTime(timetable, times.last().day, times)
                            },
                            enabled = !saving && !needsRefresh,
                            leadingContent = { Icon(Icons.Rounded.Add, contentDescription = null) },
                        )
                    }
                }
                Box(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    ActivityConflictNotice(conflict)
                }
            }
        }
    }

    if (error != 0) AlertDialog(
        onDismissRequest = { error = 0 },
        text = { Text(stringResource(error)) },
        confirmButton = {
            TextButton(onClick = {
                error = 0
            }, shapes = ButtonDefaults.shapes()) { Text(stringResource(R.string.ok)) }
        })
}

private enum class TimeField { Begin, End }

/**
 * One meeting time as a header and a card of rows: its day, start and end, adjusting it on the
 * timetable, and removing it. Tapping a time opens wheels below it in 15-minute steps, like the
 * timetable.
 */
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
    val toggle = { field: TimeField -> focus.clearFocus(); editing = if (editing == field) null else field }
    var expandedDay by remember { mutableStateOf(false) }

    SettingsSectionHeader(stringResource(R.string.activity_time_number, number))
    SettingsGroup(Modifier.animateContentSize()) {
        row {
            SettingsNavigationItem(
                text = stringResource(R.string.activity_day),
                onClick = { focus.clearFocus(); expandedDay = true },
                enabled = enabled,
                trailingContent = {
                    Box {
                        TimeValue(stringResource(DayType.fromValue(time.day)!!.fullStringValue), open = expandedDay)
                        ExpressiveMenu(
                            expanded = expandedDay,
                            onDismissRequest = { expandedDay = false },
                        ) {
                            val days = DayType.entries.sortedBy { it.value }
                            MenuGroup(index = 0, count = 1) {
                                days.forEachIndexed { index, option ->
                                    SelectableDropdownMenuItem(
                                        colors = selectableMenuItemColors(),
                                        selected = option.value == time.day,
                                        onClick = {
                                            onChange(time.move(time.begin, option.value))
                                            expandedDay = false
                                        },
                                        text = { Text(stringResource(option.fullStringValue)) },
                                        shapes = MenuDefaults.itemShape(index, days.size),
                                        selectedLeadingIcon = {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(MenuDefaults.LeadingIconSize)
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }
        row {
            SettingsNavigationItem(
                text = stringResource(R.string.activity_starts),
                onClick = { toggle(TimeField.Begin) },
                enabled = enabled,
                trailingContent = {
                    TimeValue(activityTime(time.begin), open = editing == TimeField.Begin, error = conflict)
                },
            )
        }
        if (editing == TimeField.Begin) row {
            SettingsFieldItem {
                TimeWheel(minuteOfDay = time.begin, step = 15) { newBegin ->
                    if (newBegin != time.begin) {
                        onChange(time.copy(begin = newBegin, end = if (newBegin >= time.end) (newBegin + 15).coerceAtMost(1440) else time.end))
                    }
                }
            }
        }
        row {
            SettingsNavigationItem(
                text = stringResource(R.string.activity_ends),
                onClick = { toggle(TimeField.End) },
                enabled = enabled,
                supportingText = if (conflict) stringResource(R.string.activity_conflict) else null,
                trailingContent = {
                    TimeValue(activityTime(time.end), open = editing == TimeField.End, error = conflict)
                },
            )
        }
        if (editing == TimeField.End) row {
            SettingsFieldItem {
                TimeWheel(minuteOfDay = time.end % 1440, step = 15) { picked ->
                    // 00:00 as an end time means midnight at the end of the day.
                    val newEnd = if (picked == 0) 1440 else picked
                    if (newEnd != time.end) {
                        onChange(time.copy(begin = if (newEnd <= time.begin) (newEnd - 15).coerceAtLeast(0) else time.begin, end = newEnd))
                    }
                }
            }
        }
        row {
            SettingsNavigationItem(
                text = stringResource(R.string.activity_adjust),
                onClick = onAdjust,
                enabled = enabled,
                leadingContent = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            )
        }
        if (onRemove != null) row {
            SettingsNavigationItem(
                text = stringResource(R.string.activity_remove_time),
                onClick = onRemove,
                enabled = enabled,
                leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                destructive = true,
            )
        }
    }
}

/** A row's current value: primary while its picker is open, the error color when it conflicts. */
@Composable
private fun TimeValue(text: String, open: Boolean, error: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = when {
            error -> colors.error
            open -> colors.primary
            else -> colors.onSurfaceVariant
        }
    )
}

private val activityTimesSaver = listSaver<List<ActivityTime>, Int>(
    save = { times -> times.flatMap { listOf(it.day, it.begin, it.end) } },
    restore = { values -> values.chunked(3).map { ActivityTime(it[0], it[1], it[2]) } },
)

/** Describe the activity in words; the on-device model fills in the fields below. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActivityQuickFillField(
    state: ActivityQuickFillState,
    enabled: Boolean,
    onTextChange: () -> Unit,
    onFill: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var description by rememberSaveable { mutableStateOf("") }
    val canFill = enabled && description.isNotBlank() && !state.isParsing
    SettingsGroup(modifier) {
        row {
            SettingsFieldItem {
                TextField(
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
                            LoadingIndicator(Modifier.size(24.dp))
                        } else {
                            IconButton(onClick = { onFill(description) }, enabled = canFill, shapes = IconButtonDefaults.shapes()) {
                                Icon(Icons.Outlined.AutoAwesome, stringResource(R.string.activity_quick_fill_action))
                            }
                        }
                    },
                    colors = settingsTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
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
