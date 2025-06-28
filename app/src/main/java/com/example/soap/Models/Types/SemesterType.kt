package com.example.soap.Models.Types

enum class SemesterType(val title : String): Comparable<SemesterType> {
    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn"),
    WINTER("Winter");

    companion object {
        private val customOrder: List<SemesterType> = listOf(SPRING, SUMMER, AUTUMN, WINTER)

        val customComparator: Comparator<SemesterType> = Comparator { s1, s2 ->
            customOrder.indexOf(s1).compareTo(customOrder.indexOf(s2))
        }
    }

}