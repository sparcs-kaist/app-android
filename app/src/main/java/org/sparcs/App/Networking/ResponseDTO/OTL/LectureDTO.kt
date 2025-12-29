package org.sparcs.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Enums.OTL.LectureType
import org.sparcs.App.Domain.Enums.OTL.SemesterType
import org.sparcs.App.Domain.Helpers.LocalizedString
import org.sparcs.App.Domain.Models.OTL.Department
import org.sparcs.App.Domain.Models.OTL.Lecture

data class LectureDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_en")
    val enTitle: String,

    @SerializedName("course")
    val course: Int,

    @SerializedName("old_code")
    val oldCode: String,

    @SerializedName("class_no")
    val classNumber: String,

    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("code")
    val code: String,

    @SerializedName("department")
    val department: Int,

    @SerializedName("department_code")
    val departmentCode: String,

    @SerializedName("department_name")
    val departmentName: String,

    @SerializedName("department_name_en")
    val departmentEnName: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("type_en")
    val enType: String,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("num_people")
    val numPeople: Int,

    @SerializedName("is_english")
    val isEnglish: Boolean,

    @SerializedName("credit")
    val credit: Int,

    @SerializedName("credit_au")
    val creditAu: Int,

    @SerializedName("common_title")
    val commonTitle: String,

    @SerializedName("common_title_en")
    val commonEnTitle: String,

    @SerializedName("class_title")
    val classTitle: String,

    @SerializedName("class_title_en")
    val classEnTitle: String,

    @SerializedName("review_total_weight")
    val reviewTotalWeight: Double? = 0.0,

    @SerializedName("grade")
    val grade: Double? = 0.0,

    @SerializedName("load")
    val load: Double? = 0.0,

    @SerializedName("speech")
    val speech: Double? = 0.0,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("classtimes")
    val classTimes: List<ClassTimeDTO>?,

    @SerializedName("examtimes")
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