package org.sparcs.soap.app.networking.requestDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.DayType
import org.sparcs.soap.app.domain.models.otl.LectureSearchRequest

data class LectureSearchRequestDTO(
    @SerializedName("keyword")
    val keyword: String,

    @SerializedName("type")
    val type: List<String>?,

    @SerializedName("department")
    val department: List<String>?,

    @SerializedName("level")
    val level: List<String>?,

    @SerializedName("term")
    val term: String?,

    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("day")
    val day: DayType?,

    @SerializedName("begin")
    val begin: Int?,

    @SerializedName("end")
    val end: Int?,

    @SerializedName("order")
    val order: String = "code",

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("offset")
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
                type = model.type,
                department = model.department,
                level = model.level,
                term = model.term,
                day = null,
                begin = null,
                end = null
            )
        }
    }
}