package org.sparcs.soap.App.Networking.RequestDTO.OTL

import kotlinx.serialization.SerialName
import org.sparcs.soap.App.Domain.Models.OTL.LectureSearchRequest

data class LectureSearchRequestDTO(
    @SerialName("year")
    val year: Int,

    @SerialName("semester")
    val semester: Int,

    @SerialName("keyword")
    val keyword: String,

    @SerialName("type")
    val type: String,

    @SerialName("department")
    val department: String,

    @SerialName("level")
    val level: String,

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
                type = "ALL",
                department = "ALL",
                level = "ALL",
                limit = model.limit,
                offset = model.offset
            )
        }
    }
}