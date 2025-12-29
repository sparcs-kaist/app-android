package org.sparcs.App.Domain.Models.OTL

import androidx.compose.ui.graphics.Color
import org.sparcs.App.Domain.Enums.OTL.LectureType
import org.sparcs.App.Domain.Enums.OTL.SemesterType
import org.sparcs.App.Domain.Helpers.CourseRepresentable
import org.sparcs.App.Domain.Helpers.LocalizedString
import org.sparcs.App.Domain.Helpers.TimetableColorPalette

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
    override val credit: Int,
    override val creditAu: Int,
    val capacity: Int,
    val numberOfPeople: Int,
    override val grade: Double,
    override val load: Double,
    override val speech: Double,
    val reviewTotalWeight: Double,
    val type: LectureType,
    val typeDetail: LocalizedString,
    val professors: List<Professor>,
    val classTimes: List<ClassTime>,
    val examTimes: List<ExamTime>
): CourseRepresentable {
    companion object { }
}