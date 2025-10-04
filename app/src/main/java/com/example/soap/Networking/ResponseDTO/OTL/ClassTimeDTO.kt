package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.ClassTime
import kotlinx.serialization.SerialName

data class ClassTimeDTO(
    @SerialName("building_code")
    val buildingCode: String,

    @SerialName("classroom")
    val classroom: String,

    @SerialName("classroom_en")
    val enClassroom: String,

    @SerialName("classroom_short")
    val classroomShort: String,

    @SerialName("classroom_short_en")
    val enClassroomShort: String,

    @SerialName("room_name")
    val roomName: String,

    @SerialName("day")
    val day: Int,

    @SerialName("begin")
    val begin: Int,

    @SerialName("end")
    val end: Int
) {
    fun toModel(): ClassTime = ClassTime(
        buildingCode = buildingCode,
        classroomName = LocalizedString(mapOf(
            "ko" to classroom,
            "en" to enClassroom
        )),
        classroomNameShort = LocalizedString(mapOf(
            "ko" to classroomShort,
            "en" to enClassroomShort
        )),
        roomName = roomName,
        day = DayType.fromValue(day) ?: DayType.SUN,
        begin = begin,
        end = end
    )
}