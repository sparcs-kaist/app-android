package org.sparcs.soap.App.Domain.Models.OTL

import androidx.compose.ui.graphics.Color
import org.sparcs.soap.App.Domain.Helpers.CourseRepresentable
import org.sparcs.soap.App.Domain.Helpers.TimetableColorPalette

// Background color for TimetableGridCell
val Lecture.backgroundColor: Color
    get() {
        val palette = TimetableColorPalette.palettes[0]
        val index = courseId % palette.colors.size
        return palette.colors[index]
    }

// Text colour for TimetableGridCell
val Lecture.textColor: Color
    get(){
        return  TimetableColorPalette.palettes[0].textColor
    }

data class Lecture(
    val id: Int,
    val courseId: Int,
    val classNo: String,
    val name: String,
    val subtitle: String,
    val code: String,
    val department: Department,
    val type: String,
    val capacity: Int,
    val numberOfPeople: Int,
    val credit: Int,
    val creditAu: Int,
    override val grade: Double,
    override val load: Double,
    override val speech: Double,
    val isEnglish: Boolean,
    val professors: List<Professor>,
    val classTimes: List<ClassTime>,
    val examTimes: List<ExamTime>
): CourseRepresentable {
    companion object
}

data class LectureWrapperCourse(
    val name: String,
    val code: String,
    val type: String,
    val lectures: List<Lecture>
) {
    companion object
}