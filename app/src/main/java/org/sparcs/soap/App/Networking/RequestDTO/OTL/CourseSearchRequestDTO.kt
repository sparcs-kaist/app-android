package org.sparcs.soap.App.Networking.RequestDTO.OTL

import kotlinx.serialization.SerialName
import org.sparcs.soap.App.Domain.Models.OTL.CourseSearchRequest

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