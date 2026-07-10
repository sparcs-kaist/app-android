package org.sparcs.soap.app.networking.requestDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest

data class CourseSearchRequestDTO(
    @SerializedName("keyword")
    val keyword: String,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("offset")
    val offset: Int,

    @SerializedName("type")
    val type: List<String>?,

    @SerializedName("department")
    val department: List<String>?,

    @SerializedName("level")
    val level: List<String>?,

    @SerializedName("term")
    val term: String?,
) {
    companion object {
        fun fromModel(model: CourseSearchRequest): CourseSearchRequestDTO {
            return CourseSearchRequestDTO(
                keyword = model.keyword,
                limit = model.limit,
                offset = model.offset,
                type = model.type,
                department = model.department,
                level = model.level,
                term = model.term
            )
        }
    }
}