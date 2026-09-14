package org.sparcs.soap.widgets

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.stringPreferencesKey
import org.sparcs.soap.app.domain.helpers.TimetableTheme
import org.sparcs.soap.widgets.buddyTimetableWidget.WidgetTimetableEntry
import org.sparcs.soap.widgets.buddyUpcomingClassWidget.WidgetLectureEntry

/** Each widget picks its own timetable theme in its configuration screen. */
val WIDGET_THEME_ID = stringPreferencesKey("timetable_theme_id")

fun Color.toWidgetHex(): String = String.format("#%06X", 0xFFFFFF and toArgb())

/**
 * Recolors an entry with the selected timetable theme. Entries synced before themes existed carry
 * no [WidgetLectureEntry.colorID] and keep the colors that were baked in at sync time.
 */
fun WidgetLectureEntry.themed(theme: TimetableTheme): WidgetLectureEntry = colorID?.let { id ->
    copy(bgColor = theme.colorFor(id).toWidgetHex(), textColor = theme.textColor.toWidgetHex())
} ?: this

fun WidgetTimetableEntry.themed(theme: TimetableTheme): WidgetTimetableEntry = copy(
    lecturesByDay = lecturesByDay.mapValues { (_, entries) -> entries.map { it.themed(theme) } },
    activitiesByDay = activitiesByDay.mapValues { (_, entries) -> entries.map { it.themed(theme) } },
)
