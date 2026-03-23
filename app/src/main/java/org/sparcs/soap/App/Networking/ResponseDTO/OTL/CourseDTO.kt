package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.Course

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
        type = type,
        department = department.toModel(),
        summary = summary,
        history = history.map { it.toModel() },
        classDuration = classDuration,
        expDuration = expDuration,
        credit = credit,
        creditAu = creditAu
    )
}