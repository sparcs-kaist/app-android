package com.example.soap.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SemesterDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int, // 1: Spring, 2: Summer, 3: Autumn, 4: Winter

    @SerializedName("beginning")
    val beginning: Date?,

    @SerializedName("end")
    val end: Date?,

    @SerializedName("courseDesciptionSubmission")
    val courseDescriptionSubmission: Date?,

    @SerializedName("courseRegistrationPeriodStart")
    val courseRegistrationPeriodStart: Date?,

    @SerializedName("courseRegistrationPeriodEnd")
    val courseRegistrationPeriodEnd: Date?,

    @SerializedName("courseAddDropPeriodEnd")
    val courseAddDropPeriodEnd: Date?,

    @SerializedName("courseDropDeadline")
    val courseDropDeadline: Date?,

    @SerializedName("courseEvaluationDeadline")
    val courseEvaluationDeadline: Date?,

    @SerializedName("gradePosting")
    val gradePosting: Date?
)
