package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Course
import kotlinx.serialization.SerialName

data class CourseDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("old_code")
    val code: String,

    @SerialName("department")
    val department: DepartmentDTO,

    @SerialName("type")
    val type: String,

    @SerialName("type_en")
    val enType: String,

    @SerialName("title")
    val title: String,

    @SerialName("title_en")
    val enTitle: String,

    @SerialName("summary")
    val summary: String,

    @SerialName("review_total_weight")
    val reviewTotalWeight: Double,

    @SerialName("grade")
    val grade: Double?,

    @SerialName("load")
    val load: Double?,

    @SerialName("speech")
    val speech: Double?,

    @SerialName("credit")
    val credit: Int,

    @SerialName("credit_au")
    val creditAU: Int,

    @SerialName("num_classes")
    val numClasses: Int,

    @SerialName("num_labs")
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