package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Models.OTL.LectureExam

data class LectureExamDTO(
    @SerializedName("day")
    val day: Int,

    @SerializedName("str")
    val str: String,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int
) {
    fun toModel(): LectureExam = LectureExam(
        day = DayType.fromValue(day) ?: DayType.SUN,
        description = str,
        begin = begin,
        end = end
    )
}