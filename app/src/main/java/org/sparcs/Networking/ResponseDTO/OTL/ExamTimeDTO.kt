package org.sparcs.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.Domain.Enums.OTL.DayType
import org.sparcs.Domain.Helpers.LocalizedString
import org.sparcs.Domain.Models.OTL.ExamTime

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