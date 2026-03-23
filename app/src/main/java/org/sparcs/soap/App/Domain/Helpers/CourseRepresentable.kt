package org.sparcs.soap.App.Domain.Helpers


import org.sparcs.soap.App.Domain.Models.OTL.Timetable.Companion.letters
import kotlin.math.roundToInt

interface CourseRepresentable {
    val credit: Int
    val creditAU: Int
    val grade: Double
    val load: Double
    val speech: Double
}

// Letter grade for the grade
val CourseRepresentable.gradeLetter: String
    get() = letter(grade)

// Letter grade for the load
val CourseRepresentable.loadLetter: String
    get() = letter(load)

// Letter grade for the speech
val CourseRepresentable.speechLetter: String
    get() = letter(speech)

// safely get letter grade string
private fun CourseRepresentable.letter(value: Double): String {
    val index = value.roundToInt()
    return letters.getOrElse(index) { "?" }
}

fun List<CourseRepresentable>.gradeLetter(totalCredit: Int): String {
    if (this.isEmpty()) return "?"
    val average = this.map { it.grade }.average()
    return letter(average)
}

fun List<CourseRepresentable>.loadLetter(totalCredit: Int): String {
    if (this.isEmpty()) return "?"
    val average = this.map { it.load }.average()
    return letter(average)
}

fun List<CourseRepresentable>.speechLetter(totalCredit: Int): String {
    if (this.isEmpty()) return "?"
    val average = this.map { it.speech }.average()
    return letter(average)
}

private fun letter(value: Double): String {
    val index = value.roundToInt()
    return letters.getOrElse(index) { "?" }
}

private fun CourseRepresentable.calculateWeightedAverage(value: Double): Double {
    val totalCredit = (credit + creditAU).toDouble()
    val numerator = value * totalCredit

    return if (totalCredit > 0.0) {
        numerator / totalCredit
    } else {
        0.0
    }
}