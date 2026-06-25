package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureClass

data class LectureClassDTO(
    @SerializedName("day")
    val day: Int,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int,

    @SerializedName("buildingCode")
    val buildingCode: String,

    @SerializedName("buildingName")
    val buildingName: String,

    @SerializedName("roomName")
    val roomName: String,
) {
    fun toModel(): LectureClass = LectureClass(
        day = DayType.fromValue(day) ?: DayType.SUN,
        begin = begin,
        end = end,
        buildingCode = buildingCode,
        buildingName = buildingName,
        roomName = roomName,
    )
}