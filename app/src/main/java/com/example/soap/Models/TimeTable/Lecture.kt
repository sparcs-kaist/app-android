package com.example.soap.Models.TimeTable

import androidx.compose.ui.graphics.Color
import com.example.soap.Models.Types.LectureType
import com.example.soap.Models.Types.SemesterType
import com.example.soap.Utilities.Helpers.LocalizedString
import com.example.soap.Utilities.Helpers.TimetableColorPalette
import kotlin.math.roundToInt

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
    val department: LocalizedString,
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

private fun Lecture.calculateWeightedAverage(value: Double, withCredits: Boolean = true ): Double {
      val denominator = credit + creditAu
    return if (denominator > 0) {
        value
    } else {
        0.0
    }
}

private fun Lecture.letter(forValue: Double): String {
    val index = forValue.roundToInt()
    return Timetable.letters.getOrNull(index) ?: "?"
}

val Lecture.gradeLetter: String
    get() = letter(forValue = this.grade)

val Lecture.loadLetter: String
    get() = letter(forValue = this.load)

val Lecture.speechLetter: String
    get() = letter(forValue = this.speech)