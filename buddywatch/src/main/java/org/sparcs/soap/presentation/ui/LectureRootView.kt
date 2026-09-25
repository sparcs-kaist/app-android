package org.sparcs.soap.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import org.sparcs.soap.R
import org.sparcs.soap.data.models.Timetable
import org.sparcs.soap.data.models.scheduleEntries
import org.sparcs.soap.presentation.LectureViewOption
import org.sparcs.soap.presentation.theme.SoapTheme
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun LectureRootView(
    timetable: Timetable?,
    viewOption: LectureViewOption,
    now: LocalDateTime,
    onSelectOption: (LectureViewOption) -> Unit
) {
    val today = now.toLocalDate().toString()
    val stateHolder = key(viewOption, today) { rememberSaveableStateHolder() }
    var screen by rememberSaveable(viewOption, today) { mutableStateOf(viewOption) }
    var selectedDay by rememberSaveable(viewOption, today) { mutableIntStateOf(now.dayOfWeek.value) }
    var focusedID by rememberSaveable(viewOption, today) { mutableStateOf<String?>(null) }
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var detailParent by rememberSaveable(viewOption, today) { mutableStateOf(LectureViewOption.DAY) }
    val day = DayOfWeek.of(selectedDay)
    val goBack = {
        if (showOptions) showOptions = false
        else screen = if (screen == LectureViewOption.UP_NEXT) detailParent else LectureViewOption.WEEK
    }
    val canGoBack = showOptions || screen != LectureViewOption.WEEK
    BackHandler(canGoBack, onBack = goBack)
    SwipeToDismissBox(onDismissed = goBack, hasBackground = canGoBack) { isBackground ->
        if (isBackground) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background))
        } else stateHolder.SaveableStateProvider(
            if (showOptions) "options" else "$screen-$selectedDay-${if (screen == LectureViewOption.UP_NEXT) focusedID else null}"
        ) {
            val title = day.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val showViewOptions = { showOptions = true }
            when {
                showOptions -> ViewOptionsView(viewOption) { option ->
                    showOptions = false
                    screen = option
                    selectedDay = now.dayOfWeek.value
                    focusedID = null
                    detailParent = LectureViewOption.DAY
                    onSelectOption(option)
                }
                timetable == null -> ScheduleScaffold(stringResource(R.string.app_name)) {
                    item { Text(stringResource(R.string.no_sync), textAlign = TextAlign.Center) }
                }
                screen == LectureViewOption.WEEK -> WeekTimetableView(
                    timetable, now.dayOfWeek, onShowOptions = showViewOptions
                ) {
                    selectedDay = it.value
                    screen = LectureViewOption.DAY
                }
                screen == LectureViewOption.DAY -> DayTimetableView(
                    timetable, day, onShowOptions = showViewOptions
                ) {
                    focusedID = it.id
                    detailParent = LectureViewOption.DAY
                    screen = LectureViewOption.UP_NEXT
                }
                screen == LectureViewOption.LIST -> LectureListView(
                    timetable.scheduleEntries(day), title, showViewOptions
                ) {
                    focusedID = it.id
                    detailParent = LectureViewOption.LIST
                    screen = LectureViewOption.UP_NEXT
                }
                else -> LectureTabView(timetable.scheduleEntries(day), focusedID, now, title, showViewOptions)
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Preview(device = WearDevices.LARGE_ROUND, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LectureRootViewPreview() {
    SoapTheme { LectureRootView(Timetable.mock(), LectureViewOption.UP_NEXT, LocalDateTime.of(2026, 9, 21, 9, 0)) {} }
}
