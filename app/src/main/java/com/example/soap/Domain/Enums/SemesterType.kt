package com.example.soap.Domain.Enums

enum class SemesterType(val rawValue: String, val shortCode: String) {
    SPRING("Spring", "S"),
    SUMMER("Summer", "U"),
    AUTUMN("Autumn", "F"),
    WINTER("Winter", "W");

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

        fun fromRawString(raw: String): SemesterType =
            entries.firstOrNull { it.rawValue == raw } ?: SPRING
    }
}