package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Models.OTL.Lecture

data class LectureDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("courseId")
    val courseID: Int,

    @SerializedName("classNo")
    val classNo: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("subtitle")
    val subtitle: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("department")
    val department: DepartmentDTO,

    @SerializedName("type")
    val type: String,

    @SerializedName("limitPeople")
    val limitPeople: Int,

    @SerializedName("numPeople")
    val numPeople: Int,

    @SerializedName("credit")
    val credit: Int,

    @SerializedName("creditAU")
    val creditAU: Int,

    @SerializedName("averageGrade")
    val averageGrade: Double? = 0.0,

    @SerializedName("averageLoad")
    val averageLoad: Double? = 0.0,

    @SerializedName("averageSpeech")
    val averageSpeech: Double? = 0.0,

    @SerializedName("isEnglish")
    val isEnglish: Boolean,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("classes")
    val classes: List<LectureClassDTO>,

    @SerializedName("examTimes")
    val examTimes: List<LectureExamDTO>,

    @SerializedName("classDuration")
    val classDuration: Int,

    @SerializedName("expDuration")
    val expDuration: Int
) {
    fun toModel(): Lecture = Lecture(
        id = id,
        courseID = courseID,
        section = classNo,
        name = name,
        subtitle = subtitle,
        code = code,
        department = department.toModel(),
        type = LectureType.fromString(type),
        capacity = limitPeople,
        enrolledCount = numPeople,
        credit = credit,
        creditAU = creditAU,
        grade = averageGrade ?: 0.0,
        load = averageLoad ?: 0.0,
        speech = averageSpeech ?: 0.0,
        isEnglish = isEnglish,
        professors = professors.map { it.toModel() },
        classes = classes.map { it.toModel() },
        exams = examTimes.map { it.toModel() },
        classDuration = classDuration,
        expDuration = expDuration
    )
}