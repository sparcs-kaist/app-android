package com.example.soap.Models.Types

enum class SemesterType(val title : String, val shortCode: String): Comparable<SemesterType> {
    SPRING("Spring", "S"),
    SUMMER("Summer", "U"),
    AUTUMN("Autumn", "F"),
    WINTER("Winter", "W");

    companion object {
        private val customOrder: List<SemesterType> = listOf(SPRING, SUMMER, AUTUMN, WINTER)

        val customComparator: Comparator<SemesterType> = Comparator { s1, s2 ->
            customOrder.indexOf(s1).compareTo(customOrder.indexOf(s2))
        }
    }

}