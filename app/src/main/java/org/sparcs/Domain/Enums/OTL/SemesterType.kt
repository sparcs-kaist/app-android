package org.sparcs.Domain.Enums.OTL

import org.sparcs.R

enum class SemesterType(val rawValue: Int, val shortCode: String) {
    SPRING(R.string.spring, "S"),
    SUMMER(R.string.summer, "U"),
    AUTUMN(R.string.autumn, "F"),
    WINTER(R.string.winter, "W");

    val intValue: Int
        get() = when (this) {
            SPRING -> 1
            SUMMER -> 2
            AUTUMN -> 3
            WINTER -> 4
        }
    companion object {
        fun fromRawValue(rawValue: Int): SemesterType = when (rawValue) {
            1 -> SPRING
            2 -> SUMMER
            3 -> AUTUMN
            4 -> WINTER
            else -> SPRING
        }
    }
}