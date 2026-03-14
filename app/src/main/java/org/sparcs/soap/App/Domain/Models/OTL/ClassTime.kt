package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import java.time.LocalDateTime

data class ClassTime(
    val day: DayType,
    val begin: Int,
    val end: Int,
    val buildingCode: String,
    val buildingName: String,
    val roomName: String,
){
    fun begin(now: LocalDateTime): LocalDateTime {
        val hour = begin / 60
        val minute = begin % 60

        return now.withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
    }
}