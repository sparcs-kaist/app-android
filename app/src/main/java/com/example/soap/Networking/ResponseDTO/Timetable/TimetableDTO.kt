package com.example.soap.Networking.ResponseDTO.Timetable

import com.google.gson.annotations.SerializedName

data class TimetableDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("lectures")
    val lectures: List<LectureDTO>
)

data class LectureDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("title_en")
    val titleEn: String,

    @SerializedName("course")
    val course: Int,

    @SerializedName("old_code")
    val oldCode: String,

    @SerializedName("class_no")
    val classNo: String,

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
    val departmentNameEn: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("type_en")
    val typeEn: String,

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
    val commonTitleEn: String,

    @SerializedName("class_title")
    val classTitle: String,

    @SerializedName("class_title_en")
    val classTitleEn: String,

    @SerializedName("review_total_weight")
    val reviewTotalWeight: Double,

    @SerializedName("grade")
    val grade: Double,

    @SerializedName("load")
    val load: Double,

    @SerializedName("speech")
    val speech: Double,

    @SerializedName("professors")
    val professors: List<ProfessorDTO>,

    @SerializedName("classtimes")
    val classtimes: List<ClasstimeDTO>,

    @SerializedName("examtimes")
    val examtimes: List<ExamtimeDTO>?
)

data class ProfessorDTO(
    @SerializedName("name")
    val name: String,

    @SerializedName("name_en")
    val nameEn: String,

    @SerializedName("professor_id")
    val professorId: Int,

    @SerializedName("review_total_weight")
    val reviewTotalWeight: Double
)

data class ClasstimeDTO(
    @SerializedName("building_code")
    val buildingCode: String,

    @SerializedName("classroom")
    val classroom: String,

    @SerializedName("classroom_en")
    val classroomEn: String,

    @SerializedName("classroom_short")
    val classroomShort: String,

    @SerializedName("classroom_short_en")
    val classroomShortEn: String,

    @SerializedName("room_name")
    val roomName: String,

    @SerializedName("day")
    val day: Int,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int
)

data class ExamtimeDTO(
    @SerializedName("str")
    val str: String,

    @SerializedName("str_en")
    val strEn: String,

    @SerializedName("day")
    val day: Int,

    @SerializedName("begin")
    val begin: Int,

    @SerializedName("end")
    val end: Int
)
