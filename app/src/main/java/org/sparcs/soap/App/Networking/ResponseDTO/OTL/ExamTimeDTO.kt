package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Helpers.LocalizedString
import org.sparcs.soap.App.Domain.Models.OTL.ExamTime

data class ExamTimeDTO(
    @SerializedName("str")
    val description: String,

    @SerializedName("str_en")
    val enDescription: String,

    @SerializedName("day")
    val day: Int,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int
) {
    fun toModel(): ExamTime = ExamTime(
        description = LocalizedString(
            mapOf("ko" to description, "en" to enDescription)
        ),
        day = DayType.fromValue(day) ?: DayType.SUN,
        begin = begin,
        end = end
    )
}