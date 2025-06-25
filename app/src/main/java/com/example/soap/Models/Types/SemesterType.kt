package com.example.soap.Models.Types

enum class SemesterType(val title : String): Comparable<SemesterType> {
    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn"),
    WINTER("Winter");

    companion object {
        private val order = listOf(SPRING, SUMMER, AUTUMN, WINTER)
        fun semesterIsLessThan(lhs: SemesterType, rhs: SemesterType): Boolean {
            val lhsIndex = order.indexOf(lhs)
            val rhsIndex = order.indexOf(rhs)

            if (lhsIndex == -1 || rhsIndex == -1) {
                throw IllegalArgumentException("Unknown SemesterType in comparison")
            }
            return lhsIndex < rhsIndex
        }
    }

}