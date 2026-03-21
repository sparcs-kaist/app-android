package org.sparcs.soap.App.Domain.Models.OTL

import androidx.compose.ui.graphics.Color
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import org.sparcs.soap.App.Domain.Helpers.CourseRepresentable
import org.sparcs.soap.App.Domain.Helpers.TimetableColorPalette

// Background color for TimetableGridCell
val Lecture.backgroundColor: Color
    get() {
        val palette = TimetableColorPalette.palettes[0]
        val index = courseID % palette.colors.size
        return palette.colors[index]
    }

// Text colour for TimetableGridCell
val Lecture.textColor: Color
    get(){
        return  TimetableColorPalette.palettes[0].textColor
    }

data class Lecture(
    val id: Int,
    val courseID: Int,
    val section: String,
    val name: String,
    val subtitle: String,
    val code: String,
    val department: Department,
    val type: LectureType,
    val capacity: Int,
    val enrolledCount: Int,
    override val credit: Int,
    override val creditAU: Int,
    override val grade: Double,
    override val load: Double,
    override val speech: Double,
    val isEnglish: Boolean,
    val professors: List<Professor>,
    val classes: List<LectureClass>,
    val exams: List<LectureExam>,
    val classDuration: Int,
    val expDuration: Int
): CourseRepresentable {
    companion object
}
