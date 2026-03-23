package org.sparcs.soap.App.Networking.RequestDTO.OTL

import kotlinx.serialization.SerialName
import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Models.OTL.LectureSearchRequest

data class LectureSearchRequestDTO(
    @SerialName("keyword")
    val keyword: String,

    @SerialName("type")
    val type: List<String>?,

    @SerialName("department")
    val department: List<Int>?,

    @SerialName("level")
    val level: List<Int>?,

    @SerialName("year")
    val year: Int,

    @SerialName("semester")
    val semester: Int,

    @SerialName("day")
    val day: DayType?,

    @SerialName("begin")
    val begin: Int?,

    @SerialName("end")
    val end: Int?,

    @SerialName("order")
    val order: String = "code",

    @SerialName("limit")
    val limit: Int,

    @SerialName("offset")
    val offset: Int
) {
    companion object {
        fun fromModel(model: LectureSearchRequest): LectureSearchRequestDTO {
            return LectureSearchRequestDTO(
                year = model.semester.year,
                semester = model.semester.semesterType.intValue,
                keyword = model.keyword,
                limit = model.limit,
                offset = model.offset,
                type = null,
                department = null,
                level = null,
                day = null,
                begin = null,
                end = null
            )
        }
    }
}