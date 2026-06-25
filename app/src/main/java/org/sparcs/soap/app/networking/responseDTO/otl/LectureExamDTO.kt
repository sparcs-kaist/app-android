package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureExam

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