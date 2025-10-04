package com.example.soap.Domain.Models.OTL

import androidx.compose.ui.graphics.Color
import com.example.soap.Domain.Enums.LectureType
import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Helpers.TimetableColorPalette

// Background color for TimetableGridCell
val Lecture.backgroundColor: Color
    get() {
        val palette = TimetableColorPalette.palettes[0]
        val index = course % palette.colors.size
        return palette.colors[index]
    }

// Text colour for TimetableGridCell
val Lecture.textColor: Color
    get(){
        return  TimetableColorPalette.palettes[0].textColor
    }

data class Lecture(
    val id: Int,
    val course: Int,
    val code: String,
    val section: String?,
    val year: Int,
    val semester: SemesterType,
    val title: LocalizedString,
    val department: Department,
    val isEnglish: Boolean,
    val credit: Int,
    val creditAu: Int,
    val capacity: Int,
    val numberOfPeople: Int,
    val grade: Double,
    val load: Double,
    val speech: Double,
    val reviewTotalWeight: Double,
    val type: LectureType,
    val typeDetail: LocalizedString,
    val professors: List<Professor>,
    val classTimes: List<ClassTime>,
    val examTimes: List<ExamTime>
){
    companion object{}
}