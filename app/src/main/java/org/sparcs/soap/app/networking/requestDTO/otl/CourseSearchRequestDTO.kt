package org.sparcs.soap.app.networking.requestDTO.otl

import kotlinx.serialization.SerialName
import org.sparcs.soap.app.domain.models.otl.CourseSearchRequest

data class CourseSearchRequestDTO(
    @SerialName("keyword")
    val keyword: String,

    @SerialName("limit")
    val limit: Int,

    @SerialName("offset")
    val offset: Int,
) {
    companion object {
        fun fromModel(model: CourseSearchRequest): CourseSearchRequestDTO {
            return CourseSearchRequestDTO(
                keyword = model.keyword,
                limit = model.limit,
                offset = model.offset
            )
        }
    }
}