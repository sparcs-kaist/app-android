package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Course
import com.google.gson.annotations.SerializedName

data class CourseDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("old_code")
    val code: String,

    @SerializedName("department")
    val department: DepartmentDTO,

    @SerializedName("type")
    val type: String,

    @SerializedName("type_en")
    val enType: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_en")
    val enTitle: String,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("review_total_weight")
    val reviewTotalWeight: Double,

    @SerializedName("grade")
    val grade: Double?,

    @SerializedName("load")
    val load: Double?,

    @SerializedName("speech")
    val speech: Double?,

    @SerializedName("credit")
    val credit: Int,

    @SerializedName("credit_au")
    val creditAU: Int,

    @SerializedName("num_classes")
    val numClasses: Int,

    @SerializedName("num_labs")
    val numLabs: Int
) {
    fun toModel(): Course = Course(
        id = id,
        code = code,
        department = department.toModel(),
        type = LocalizedString(
            mapOf("ko" to type, "en" to enType)
        ),
        title = LocalizedString(
            mapOf("ko" to title, "en" to enTitle)
        ),
        summary = summary,
        reviewTotalWeight = reviewTotalWeight,
        grade = grade ?: 0.0,
        load = load ?: 0.0,
        speech = speech ?: 0.0,
        credit = credit,
        creditAu = creditAU,
        numClasses = numClasses,
        numLabs = numLabs
    )
}