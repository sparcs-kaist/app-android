package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Helpers.LocalizedString
import java.time.LocalDateTime

data class ClassTime(
    val buildingCode: String,
    val classroomName: LocalizedString,
    val classroomNameShort: LocalizedString,
    val roomName: String,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
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