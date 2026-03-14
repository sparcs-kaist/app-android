package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.Course

data class CourseClassDTO(
    @SerializedName("lectureId")
    val lectureId: Int,

    @SerializedName("subtitle")
    val subtitle: String,

    @SerializedName("classNo")
    val classNo: String,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,
)

data class CourseHistoryDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: String,

    @SerializedName("classes")
    val classes: List<CourseClassDTO>,
)

data class CourseDTO (
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

    @SerializedName("history")
    val history: List<CourseHistoryDTO>,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("classDuration")
    val classDuration: Int,

    @SerializedName("expDuration")
    val expDuration: Int,

    @SerializedName("credit")
    val credit: Int,

    @SerializedName("creditAu")
    val creditAu: Int,
) {
    fun toModel(): Course = Course(
        id = id,
        name = name,
        code = code,
        type = LectureType.fromString(type),
        department = department.toModel(),
        summary = summary,
        classDuration = classDuration,
        expDuration = expDuration,
        credit = credit,
        creditAu = creditAu
    )
}