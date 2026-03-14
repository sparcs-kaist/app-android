package org.sparcs.soap.App.Domain.Helpers


import org.sparcs.soap.App.Domain.Models.OTL.Timetable.Companion.letters
import kotlin.math.roundToInt

interface CourseRepresentable {
    val grade: Double
    val load: Double
    val speech: Double
}

val CourseRepresentable.gradeLetter: String
    get() = letter(grade)

val CourseRepresentable.loadLetter: String
    get() = letter(load)

val CourseRepresentable.speechLetter: String
    get() = letter(speech)

// safely get letter grade string
private fun CourseRepresentable.letter(value: Double): String {
    val index = value.roundToInt()
    return letters.getOrElse(index) { "?" }
}