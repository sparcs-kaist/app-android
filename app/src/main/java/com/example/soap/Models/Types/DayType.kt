package com.example.soap.Models.Types

enum class DayType(val value: Int): Comparable<DayType> {
    SUN(0),
    MON(1),
    TUE(2),
    WED(3),
    THU(4),
    FRI(5),
    SAT(6);

    val stringValue: String
        get() = when (this) {
            SUN -> "Sun"
            MON -> "Mon"
            TUE -> "Tue"
            WED -> "Wed"
            THU -> "Thu"
            FRI -> "Fri"
            SAT -> "Sat"
        }

    val id: String
        get() = stringValue

    companion object {
        fun DayIsLessThan(lhs: DayType, rhs: DayType): Boolean {
            return lhs.value < rhs.value
        }
    }
}


