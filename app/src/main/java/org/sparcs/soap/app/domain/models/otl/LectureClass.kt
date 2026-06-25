package org.sparcs.soap.app.domain.models.otl

import org.sparcs.soap.app.domain.enums.otl.DayType
import java.time.LocalDateTime

data class LectureClass(
    val day: DayType,
    val begin: Int,
    val end: Int,
    val buildingCode: String,
    val buildingName: String,
    val roomName: String,
){
    val location: String
        get() = "$buildingCode $roomName"

    val duration: Int
        get() = end - begin

    fun begin(now: LocalDateTime): LocalDateTime {
        val hour = begin / 60
        val minute = begin % 60

        return now.withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
    }
}