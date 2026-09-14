package org.sparcs.soap.app.features.timetable.activity

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
internal fun activityTime(minutes: Int): String {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    if (minutes == 1440) return stringResource(R.string.activity_midnight)
    return LocalTime.of(minutes / 60, minutes % 60)
        .format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a"))
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
    val initial = remember { activity?.draft() ?: ActivityDraft.initial(timetable, (LocalDate.now().dayOfWeek.value - 1).let { if (it < 5) it else 0 }) }
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var location by rememberSaveable { mutableStateOf(initial.location) }
    var day by rememberSaveable { mutableIntStateOf(initial.day) }
    var begin by rememberSaveable { mutableIntStateOf(initial.begin) }
    var end by rememberSaveable { mutableIntStateOf(initial.end) }
    var adjusting by rememberSaveable { mutableStateOf(false) }
    var choosingDay by rememberSaveable { mutableStateOf(false) }
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
        ActivityTimetableCreationView(timetable, draft, activity?.id, onChange = update, onBack = { adjusting = false })
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (activity == null) R.string.activity_new else R.string.activity_edit)) },
                navigationIcon = { IconButton(onClick = onClose, enabled = !saving) { Icon(Icons.Default.Close, stringResource(R.string.activity_close)) } },
                actions = {
                    if (saving) CircularProgressIndicator(Modifier.padding(16.dp).size(24.dp), strokeWidth = 2.dp)
                    else TextButton(enabled = needsRefresh || (draft.isValid && !conflict), onClick = {
                        focus.clearFocus()
                        scope.launch {
                            saving = true
                            try {
                                if (needsRefresh) onRefresh() else onSave(draft)
                                onClose()
                            } catch (e: CancellationException) { throw e
                            } catch (e: Exception) {
                                error = when (e) {
                                    is ActivityConflictException -> R.string.activity_conflict
                                    is ActivityRefreshRequiredException -> { needsRefresh = true; R.string.activity_saved_refresh }
                                    else -> R.string.activity_save_error
                                }
                            } finally { saving = false }
                        }
                    }) { Text(stringResource(if (needsRefresh) R.string.activity_refresh else R.string.activity_save)) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(timetableName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.activity_title)) },
                modifier = Modifier.fillMaxWidth(), enabled = !saving && !needsRefresh, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.activity_location)) },
                leadingIcon = { Icon(Icons.Default.Place, null) }, modifier = Modifier.fillMaxWidth(), enabled = !saving && !needsRefresh,
                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
            HorizontalDivider()
            Column {
                ListItem(headlineContent = { Text(stringResource(R.string.activity_day)) },
                    trailingContent = { Text(stringResource(DayType.fromValue(day)!!.stringValue), color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { Icon(Icons.Default.DateRange, null) }, modifier = Modifier.clickable(enabled = !saving && !needsRefresh) { focus.clearFocus(); choosingDay = true })
                ListItem(headlineContent = { Text(stringResource(R.string.activity_starts)) }, trailingContent = { Text(activityTime(begin), color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { Icon(Icons.Default.Schedule, null) }, modifier = Modifier.clickable(enabled = !saving && !needsRefresh) { focus.clearFocus(); timeField = 1 })
                ListItem(headlineContent = { Text(stringResource(R.string.activity_ends)) }, trailingContent = { Text(activityTime(end), color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { Spacer(Modifier.size(24.dp)) }, modifier = Modifier.clickable(enabled = !saving && !needsRefresh) { focus.clearFocus(); timeField = 2 })
            }
            FilledTonalButton(onClick = { focus.clearFocus(); adjusting = true }, enabled = !saving && !needsRefresh, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.activity_adjust))
            }
            ActivityConflictNotice(conflict)
            Spacer(Modifier.height(16.dp))
        }
    }
    if (choosingDay) AlertDialog(onDismissRequest = { choosingDay = false }, title = { Text(stringResource(R.string.activity_day)) },
        text = { Column { DayType.entries.sortedBy { it.value }.forEach { option ->
            Row(Modifier.fillMaxWidth().clickable { day = option.value; choosingDay = false }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = day == option.value, onClick = { day = option.value; choosingDay = false })
                Text(stringResource(option.stringValue))
            }
        } } }, confirmButton = { TextButton(onClick = { choosingDay = false }) { Text(stringResource(R.string.activity_close)) } })
    if (timeField != 0) {
        val current = if (timeField == 1) begin else end % 1440
        val state = rememberTimePickerState(current / 60, current % 60, DateFormat.is24HourFormat(LocalContext.current))
        val configuration = LocalConfiguration.current
        var keyboard by rememberSaveable(timeField) { mutableStateOf(configuration.screenHeightDp < 500) }
        AlertDialog(onDismissRequest = { timeField = 0 }, title = { Text(stringResource(if (timeField == 1) R.string.activity_starts else R.string.activity_ends)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (keyboard) TimeInput(state) else TimePicker(state, layoutType = TimePickerLayoutType.Vertical)
                    TextButton(onClick = { keyboard = !keyboard }) { Text(stringResource(if (keyboard) R.string.activity_clock else R.string.activity_keyboard)) }
                }
            }, confirmButton = { TextButton(onClick = {
                val minutes = state.hour * 60 + state.minute
                update(if (timeField == 1) draft.move(minutes) else draft.resizeEnd(if (minutes == 0) 1440 else minutes))
                timeField = 0
            }) { Text(stringResource(R.string.ok)) } }, dismissButton = { TextButton(onClick = { timeField = 0 }) { Text(stringResource(R.string.activity_close)) } })
    }
    if (error != 0) AlertDialog(onDismissRequest = { error = 0 }, text = { Text(stringResource(error)) },
        confirmButton = { TextButton(onClick = { error = 0 }) { Text(stringResource(R.string.ok)) } })
}
