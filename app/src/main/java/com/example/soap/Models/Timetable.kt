package com.example.soap.Models

import androidx.compose.ui.graphics.Color
import com.example.soap.Models.Types.DayType
import com.example.soap.Utilities.Mocks.ClassTime


data class LectureItem(
    val id: Int,
    val lecture: Lecture,
    val index: Int
)

data class Lecture(
    val id: Int,
    val course: Int,
    val title: String,
    val credit: Int,
    val creditAu: Int,
    val grade: Double,
    val load: Double,
    val speech: Double,
    val reviewTotalWeight: Double,
//    val type: LectureType
    val professors: List<Professor>,
    val classTimes: List<ClassTime>,
    val examTimes: List<ExamTime>,

    // Background colour for TimetableGridCell
    var background: Color,

    // Text colour for TimetableGridCell
    var textColor: Color
)

data class Professor(
    val id: Int,
    val name: String,
    val reviewTotalWeight: Double
)

data class ClassTime(
    val classroomName: String,
    val classroomNameShort: String,
    val roomName: String,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)

data class ExamTime(
    val str: String,
    val day: DayType,
    val begin: Int,
    val end: Int,
    var duration: Int = (end - begin)
)