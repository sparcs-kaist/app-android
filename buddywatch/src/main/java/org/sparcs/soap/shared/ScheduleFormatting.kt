package org.sparcs.soap.shared

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import org.sparcs.soap.R
import org.sparcs.soap.data.models.LectureClass
import org.sparcs.soap.data.models.dayCode
import java.time.DayOfWeek
import java.time.LocalDateTime

fun String?.scheduleColor(): Color =
    runCatching { this?.let { Color(it.toColorInt()) } }.getOrNull() ?: Color(0xFF4A90E2)

fun LectureClass.statusString(context: Context, now: LocalDateTime): String {
    val dayIndex = DayOfWeek.entries.indexOfFirst { it.dayCode == day }
    val dayOffset = (dayIndex - (now.dayOfWeek.value - 1)) * 1440
    val currentMinutes = now.hour * 60 + now.minute
    val beginDelta = dayOffset + begin - currentMinutes
    val endDelta = dayOffset + end - currentMinutes
    return when {
        beginDelta > 0 -> context.getString(R.string.schedule_in, formatMinutes(context, beginDelta))
        endDelta <= 0 -> context.getString(R.string.schedule_ago, formatMinutes(context, -endDelta))
        else -> context.getString(R.string.comp_ongoing)
    }
}

private fun formatMinutes(context: Context, minutes: Int): String {
    val days = minutes / 1440
    val hours = minutes % 1440 / 60
    val remainder = minutes % 60
    return when {
        days > 0 && hours > 0 -> context.getString(R.string.duration_days_hours, days, hours)
        days > 0 -> context.getString(R.string.duration_days, days)
        hours > 0 && remainder > 0 -> context.getString(R.string.duration_hours_minutes, hours, remainder)
        hours > 0 -> context.getString(R.string.duration_hours, hours)
        else -> context.getString(R.string.duration_minutes, remainder)
    }
}
