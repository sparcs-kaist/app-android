package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Models.OTL.ClassTime

data class ClassTimeDTO(
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
    fun toModel(): ClassTime = ClassTime(
        day = DayType.fromValue(day) ?: DayType.SUN,
        begin = begin,
        end = end,
        buildingCode = buildingCode,
        buildingName = buildingName,
        roomName = roomName,
    )
}