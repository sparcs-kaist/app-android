package org.sparcs.soap.app.domain.enums.otl

import org.sparcs.soap.R

enum class DayType(val value: Int, val stringValue: Int, val fullStringValue: Int) : Comparable<DayType> {
    SUN(6, R.string.sun, R.string.sunday),
    MON(0, R.string.mon, R.string.monday),
    TUE(1, R.string.tue, R.string.tuesday),
    WED(2, R.string.wed, R.string.wednesday),
    THU(3, R.string.thu, R.string.thursday),
    FRI(4, R.string.fri, R.string.friday),
    SAT(5, R.string.sat, R.string.saturday);

    val id: Int get() = stringValue


    companion object {
        fun fromValue(value: Int): DayType? =
            entries.find { it.value == value }

        fun weekdays() = listOf(MON, TUE, WED, THU, FRI)
    }
}


