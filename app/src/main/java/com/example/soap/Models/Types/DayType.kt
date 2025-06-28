package com.example.soap.Models.Types

enum class DayType(val value: Int, val stringValue: String) : Comparable<DayType> {
    SUN(0, "Sun"),
    MON(1, "Mon"),
    TUE(2, "Tue"),
    WED(3, "Wed"),
    THU(4, "Thu"),
    FRI(5, "Fri"),
    SAT(6, "Sat");

    val id: String get() = stringValue


    companion object {
        fun fromValue(value: Int): DayType? =
            entries.find { it.value == value }
    }
}



