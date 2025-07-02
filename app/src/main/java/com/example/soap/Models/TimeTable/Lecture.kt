package com.example.soap.Models.TimeTable

import androidx.compose.ui.graphics.Color
import com.example.soap.Models.Types.LectureType
import com.example.soap.Utilities.Helpers.LocalizedString
import kotlin.math.roundToInt


data class Lecture(
    val id: Int,
    val course: Int,
    val code: String,
    val section: String?,
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
    val examTimes: List<ExamTime>,

    // Background color for TimetableGridCell
    val backgroundColor: Color = Color.Gray,

    // Text colour for TimetableGridCell
    var textColor: Color = Color.Gray
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