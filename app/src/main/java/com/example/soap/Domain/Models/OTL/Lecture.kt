package com.example.soap.Domain.Models.OTL

import androidx.compose.ui.graphics.Color
import com.example.soap.Domain.Enums.LectureType
import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Helpers.LocalizedString
import com.example.soap.Domain.Helpers.TimetableColorPalette
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

    companion object {
        val letters = listOf("?", "F", "D", "C", "B", "A")
    }
//TODo - swift에는 이런거 없음..
    // Weighted average 계산
    private fun calculateWeightedAverage(value: Double, withCredits: Boolean = true): Double {
        val numerator = value * (credit + creditAu)
        val denominator = credit + creditAu
        return if (denominator > 0) numerator / denominator else 0.0
    }

    // 단순 Letter (A~F)
    val gradeLetter: String
        get() = letters.getOrNull(calculateWeightedAverage(grade).roundToInt()) ?: "?"

    val loadLetter: String
        get() = letters.getOrNull(calculateWeightedAverage(load).roundToInt()) ?: "?"

    val speechLetter: String
        get() = letters.getOrNull(calculateWeightedAverage(speech).roundToInt()) ?: "?"

    // Letter + 세부 +/- 표시
    val gradeLetterWithSign: String
        get() = letterWithSign(grade)

    val loadLetterWithSign: String
        get() = letterWithSign(load)

    val speechLetterWithSign: String
        get() = letterWithSign(speech)

    private fun letterWithSign(value: Double): String {
        val avg = calculateWeightedAverage(value)
        val baseIndex = avg.toInt().coerceIn(1, letters.lastIndex)
        val baseLetter = letters[baseIndex]

        val fraction = avg - avg.toInt()
        val sign = when {
            fraction >= 0.66 -> "+"
            fraction <= 0.33 -> "-"
            else -> "0"
        }

        return baseLetter + sign
    }
}