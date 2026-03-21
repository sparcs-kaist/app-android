package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.CourseSummary

data class CourseSummaryDTO(
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
    fun toModel(): CourseSummary = CourseSummary(
        id = id,
        name = name,
        code = code,
        type = LectureType.fromString(type),
        summary = summary,
        open = open,
        completed = completed,
        department = department.toModel(),
        professors = professors.map { it.toModel() }
    )
}