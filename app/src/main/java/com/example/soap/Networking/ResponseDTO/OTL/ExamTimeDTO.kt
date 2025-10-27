package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.ExamTime
import com.google.gson.annotations.SerializedName

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