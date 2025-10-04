package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Enums.LectureType
import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Models.OTL.Department
import com.example.soap.Domain.Models.OTL.Lecture
import kotlinx.serialization.SerialName

data class LectureDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String,

    @SerialName("title_en")
    val enTitle: String,

    @SerialName("course")
    val course: Int,

    @SerialName("old_code")
    val oldCode: String,

    @SerialName("class_no")
    val classNumber: String,

    @SerialName("year")
    val year: Int,

    @SerialName("semester")
    val semester: Int,

    @SerialName("code")
    val code: String,

    @SerialName("department")
    val department: Int,

    @SerialName("department_code")
    val departmentCode: String,

    @SerialName("department_name")
    val departmentName: String,

    @SerialName("department_name_en")
    val departmentEnName: String,

    @SerialName("type")
    val type: String,

    @SerialName("type_en")
    val enType: String,

    @SerialName("limit")
    val limit: Int,

    @SerialName("num_people")
    val numPeople: Int,

    @SerialName("is_english")
    val isEnglish: Boolean,

    @SerialName("credit")
    val credit: Int,

    @SerialName("credit_au")
    val creditAu: Int,

    @SerialName("common_title")
    val commonTitle: String,

    @SerialName("common_title_en")
    val commonEnTitle: String,

    @SerialName("class_title")
    val classTitle: String,

    @SerialName("class_title_en")
    val classEnTitle: String,

    @SerialName("review_total_weight")
    val reviewTotalWeight: Double? = 0.0,

    @SerialName("grade")
    val grade: Double? = 0.0,

    @SerialName("load")
    val load: Double? = 0.0,

    @SerialName("speech")
    val speech: Double? = 0.0,

    @SerialName("professors")
    val professors: List<ProfessorDTO>,

    @SerialName("classtimes")
    val classTimes: List<ClassTimeDTO>?,

    @SerialName("examtimes")
    val examTimes: List<ExamTimeDTO>?
) {
    fun toModel(): Lecture = Lecture(
        id = id,
        course = course,
        code = code,
        section = classNumber,
        year = year,
        semester = SemesterType.fromRawValue(semester),
        title = LocalizedString(mapOf("ko" to title, "en" to enTitle)),
        department = Department(
            id = department,
            name = LocalizedString(mapOf("ko" to departmentName, "en" to departmentEnName)),
            code = code
        ),
        isEnglish = isEnglish,
        credit = credit,
        creditAu = creditAu,
        capacity = limit,
        numberOfPeople = numPeople,
        grade = grade ?: 0.0,
        load = load ?: 0.0,
        speech = speech ?: 0.0,
        reviewTotalWeight = reviewTotalWeight ?: 0.0,
        type = LectureType.fromRawValue(enType),
        typeDetail = LocalizedString(mapOf("ko" to type, "en" to enType)),
        professors = professors.map { it.toModel() },
        classTimes = classTimes?.map { it.toModel() } ?: emptyList(),
        examTimes = examTimes?.map { it.toModel() } ?: emptyList()
    )
}