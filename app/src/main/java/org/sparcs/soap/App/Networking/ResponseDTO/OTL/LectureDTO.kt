package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.Lecture
import org.sparcs.soap.App.Domain.Models.OTL.LectureWrapperCourse

data class LectureDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("courseId")
    val courseId: Int,

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

    @SerializedName("creditAu")
    val creditAu: Int,

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

    @SerializedName("classDuration")
    val classDuration: Int,

    @SerializedName("expDuration")
    val expDuration: Int,

    @SerializedName("classes")
    val classes: List<ClassTimeDTO>,

    @SerializedName("examTimes")
    val examTimes: List<ExamTimeDTO>
) {
    fun toModel(): Lecture = Lecture(
        id = id,
        courseId = courseId,
        classNo = classNo,
        name = name,
        subtitle = subtitle,
        code = code,
        department = department.toModel(),
        type = type,
        capacity = limitPeople,
        numberOfPeople = numPeople,
        credit = credit,
        creditAu = creditAu,
        grade = averageGrade ?: 0.0,
        load = averageLoad ?: 0.0,
        speech = averageSpeech ?: 0.0,
        isEnglish = isEnglish,
        professors = professors.map { it.toModel() },
        classTimes = classes.map { it.toModel() },
        examTimes = examTimes.map { it.toModel() }
    )
}

data class LectureWrapperCourseDTO(
    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("lectures")
    val lectures: List<LectureDTO>,

    @SerializedName("completed")
    val completed: Boolean
) {
    fun toModel(): LectureWrapperCourse {
        return LectureWrapperCourse(
            name = name,
            code = code,
            type = type,
            lectures = lectures.map { it.toModel() },
        )
    }
}

data class LectureSearchResponseDTO(
    @SerializedName("courses")
    val courses: List<LectureWrapperCourseDTO>,

    @SerializedName("totalCount")
    val totalCount: Int
)