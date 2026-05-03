package org.sparcs.soap.Widgets.BuddyDDayWidget.UI

import androidx.compose.runtime.Composable
import androidx.glance.LocalContext
import org.sparcs.soap.R
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayType
import org.sparcs.soap.Widgets.BuddyDDayWidget.DDayWidgetEntry

fun isFinished(entry: DDayWidgetEntry): Boolean {
    return entry.type == DDayType.END_OF_SEMESTER && entry.days < 0
}
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
        else -> ""
    }
}
