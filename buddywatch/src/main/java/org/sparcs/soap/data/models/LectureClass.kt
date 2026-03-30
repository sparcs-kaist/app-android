package org.sparcs.soap.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LectureClass(
    val day: String, // MON, TUE, etc.
    val begin: Int,
    val end: Int,
    @SerialName("location") val location: String
) {
    companion object {
        fun mock(
            day: String = "MON",
            begin: Int = 900,
            end: Int = 1030,
            location: String = "정보전자공학동 101호"
        ) = LectureClass(
            day = day,
            begin = begin,
            end = end,
            location = location
        )

        fun mockList() = listOf(
            mock(day = "MON", begin = 900, end = 1030),
            mock(day = "WED", begin = 900, end = 1030)
        )
    }
}