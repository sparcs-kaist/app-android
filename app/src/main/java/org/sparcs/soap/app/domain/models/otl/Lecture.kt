package org.sparcs.soap.app.domain.models.otl

import androidx.compose.ui.graphics.Color
import org.sparcs.soap.app.domain.enums.otl.LectureType
import org.sparcs.soap.app.domain.helpers.CourseRepresentable
import org.sparcs.soap.app.domain.helpers.TimetableColorPalette

// Background color for TimetableGridCell
val Lecture.backgroundColor: Color
    get() {
        val palette = TimetableColorPalette.palettes[0]
        val index = courseID % palette.colors.size
        return palette.colors[index]
    }

// Text color for TimetableGridCell
val textColor: Color
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
