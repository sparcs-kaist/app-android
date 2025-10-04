package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.SemesterEventDate
import com.example.soap.Shared.Extensions.toDate
import kotlinx.serialization.SerialName
import java.util.Date

data class SemesterDTO(
    @SerialName("year")
    val year: Int,

    @SerialName("semester")
    val semester: Int, // 1: Spring, 2: Summer, 3: Autumn, 4: Winter

    @SerialName("beginning")
    val beginning: String,

    @SerialName("end")
    val end: String,

    @SerialName("courseDesciptionSubmission")
    val courseDescriptionSubmission: String?,

    @SerialName("courseRegistrationPeriodStart")
    val courseRegistrationPeriodStart: String?,

    @SerialName("courseRegistrationPeriodEnd")
    val courseRegistrationPeriodEnd: String?,

    @SerialName("courseAddDropPeriodEnd")
    val courseAddDropPeriodEnd: String?,

    @SerialName("courseDropDeadline")
    val courseDropDeadline: String?,

    @SerialName("courseEvaluationDeadline")
    val courseEvaluationDeadline: String?,

    @SerialName("gradePosting")
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