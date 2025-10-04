package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.ExamTime
import kotlinx.serialization.SerialName

data class ExamTimeDTO(
    @SerialName("str")
    val description: String,

    @SerialName("str_en")
    val enDescription: String,

    @SerialName("day")
    val day: Int,

    @SerialName("begin")
    val begin: Int,

    @SerialName("end")
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