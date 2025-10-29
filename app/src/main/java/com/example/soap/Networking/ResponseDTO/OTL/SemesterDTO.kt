package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.SemesterEventDate
import com.example.soap.Shared.Extensions.toDate
import com.google.gson.annotations.SerializedName
import java.util.Date

data class SemesterDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int, // 1: Spring, 2: Summer, 3: Autumn, 4: Winter

    @SerializedName("beginning")
    val beginning: String,

    @SerializedName("end")
    val end: String,

    @SerializedName("courseDesciptionSubmission")
    val courseDescriptionSubmission: String?,

    @SerializedName("courseRegistrationPeriodStart")
    val courseRegistrationPeriodStart: String?,

    @SerializedName("courseRegistrationPeriodEnd")
    val courseRegistrationPeriodEnd: String?,

    @SerializedName("courseAddDropPeriodEnd")
    val courseAddDropPeriodEnd: String?,

    @SerializedName("courseDropDeadline")
    val courseDropDeadline: String?,

    @SerializedName("courseEvaluationDeadline")
    val courseEvaluationDeadline: String?,

    @SerializedName("gradePosting")
    val gradePosting: String?
) {
    fun toModel(): Semester = Semester(
        year = year,
        semesterType = SemesterType.fromRawValue(semester),
        beginDate = beginning.toDate() ?: Date(),
        endDate = end.toDate() ?: Date(),
        eventDate = SemesterEventDate(
            registrationPeriodStartDate = courseRegistrationPeriodStart?.toDate(),
            registrationPeriodEndDate = courseRegistrationPeriodEnd?.toDate(),
            addDropPeriodEndDate = courseAddDropPeriodEnd?.toDate(),
            dropDeadlineDate = courseDropDeadline?.toDate(),
            evaluationDeadlineDate = courseEvaluationDeadline?.toDate(),
            gradePostingDate = gradePosting?.toDate()
        )
    )
}