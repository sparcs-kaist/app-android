package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Models.OTL.ExamTime

data class ExamTimeDTO(
    @SerializedName("day")
    val day: Int,

    @SerializedName("str")
    val str: String,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int
) {
    fun toModel(): ExamTime = ExamTime(
        day = DayType.fromValue(day) ?: DayType.SUN,
        str = str,
        begin = begin,
        end = end
    )
}