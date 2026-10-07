package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.DayType

data class LectureTimeFilter(val day: DayType? = null, val begin: Int? = null, val end: Int? = null) {
    val isEmpty get() = day == null && begin == null && end == null
    companion object {
        val selectableTimes = (8 * 60..23 * 60 step 30).toList()
    }
}
