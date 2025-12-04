package com.sparcs.soap.Domain.Enums.OTL

import com.sparcs.soap.R

enum class DayType(val value: Int, val stringValue: Int) : Comparable<DayType> {
    SUN(6, R.string.sun),
    MON(0, R.string.mon),
    TUE(1, R.string.tue),
    WED(2, R.string.wed),
    THU(3, R.string.thu),
    FRI(4, R.string.fri),
    SAT(5, R.string.sat);

    val id: Int get() = stringValue


    companion object {
        fun fromValue(value: Int): DayType? =
            entries.find { it.value == value }

        fun weekdays() = listOf(MON, TUE, WED, THU, FRI)
    }
}


