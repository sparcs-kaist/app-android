package com.sparcs.soap.Networking.ResponseDTO.OTL

import com.sparcs.soap.Domain.Enums.OTL.DayType
import com.sparcs.soap.Domain.Helpers.LocalizedString
import com.sparcs.soap.Domain.Models.OTL.ClassTime
import com.google.gson.annotations.SerializedName

data class ClassTimeDTO(
    @SerializedName("building_code")
    val buildingCode: String,

    @SerializedName("classroom")
    val classroom: String,

    @SerializedName("classroom_en")
    val enClassroom: String,

    @SerializedName("classroom_short")
    val classroomShort: String,

    @SerializedName("classroom_short_en")
    val enClassroomShort: String,

    @SerializedName("room_name")
    val roomName: String,

    @SerializedName("day")
    val day: Int,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
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