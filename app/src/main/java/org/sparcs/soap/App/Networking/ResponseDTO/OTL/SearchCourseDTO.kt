package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.SearchCourse

data class SearchCourseDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("department")
    val department: DepartmentDTO,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("open")
    val open: Boolean,

    @SerializedName("completed")
    val completed: Boolean
) {
    fun toModel(): SearchCourse = SearchCourse(
        id = id,
        name = name,
        code = code,
        type = LectureType.fromString(type),
        summary = summary
    )
}

data class SearchCourseResponseDTO(
    @SerializedName("courses")
    val courses: List<SearchCourseDTO>,

    @SerializedName("totalCount")
    val totalCount: Int
)