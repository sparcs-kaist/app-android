package org.sparcs.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Enums.OTL.SemesterType
import org.sparcs.App.Domain.Models.OTL.Semester
import org.sparcs.App.Domain.Models.OTL.SemesterEventDate
import org.sparcs.App.Shared.Extensions.toDate
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