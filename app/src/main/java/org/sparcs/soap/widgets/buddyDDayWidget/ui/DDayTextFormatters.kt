package org.sparcs.soap.widgets.buddyDDayWidget.ui

import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import org.sparcs.soap.R
import org.sparcs.soap.widgets.buddyDDayWidget.DDayType
import org.sparcs.soap.widgets.buddyDDayWidget.DDayWidgetEntry

@Composable
fun formatDDay(days: Int): String {
    val context = LocalContext.current
    return when {
        days == 0 -> context.getString(R.string.d_day_widget_d_day)
        days > 0 -> context.getString(R.string.d_day_widget_d_minus, days)
        else -> context.getString(R.string.d_day_widget_d_plus, -days)
    }
}

@Composable
fun subtitleText(entry: DDayWidgetEntry): String {
    val context = LocalContext.current
    return when (entry.type) {
        DDayType.START_OF_SEMESTER -> context.getString(R.string.d_day_widget_starts_in)
        DDayType.END_OF_SEMESTER -> context.getString(R.string.d_day_widget_ends_in)
        DDayType.SEMESTER_ENDED -> context.getString(R.string.d_day_widget_semester_ended)
        else -> ""
    }
}
