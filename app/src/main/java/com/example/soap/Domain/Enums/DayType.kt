package com.example.soap.Domain.Enums

enum class DayType(val value: Int, val stringValue: String) : Comparable<DayType> {
    SUN(6, "Sun"),
    MON(0, "Mon"),
    TUE(1, "Tue"),
    WED(2, "Wed"),
    THU(3, "Thu"),
    FRI(4, "Fri"),
    SAT(5, "Sat");

    val id: String get() = stringValue


    companion object {
        fun fromValue(value: Int): DayType? =
            entries.find { it.value == value }

        fun weekdays() = listOf(MON, TUE, WED, THU, FRI)
    }
}


