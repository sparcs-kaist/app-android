package org.sparcs.soap.shared

import java.util.Locale

fun formatTimeRange(begin: Int, end: Int): String {
    fun Int.toTime(): String = String.format(Locale.ROOT, "%02d:%02d", (this / 60) % 24, this % 60)
    return "${begin.toTime()} - ${end.toTime()}"
}