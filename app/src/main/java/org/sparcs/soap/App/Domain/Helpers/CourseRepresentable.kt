package org.sparcs.soap.App.Domain.Helpers


import org.sparcs.soap.App.Domain.Models.OTL.Timetable.Companion.letters
import kotlin.math.roundToInt

interface CourseRepresentable {
    val credit: Int
    val creditAu: Int
    val grade: Double
    val load: Double
    val speech: Double
}
// Letter grade for the grade
val CourseRepresentable.gradeLetter: String
    get() = letter(calculateWeightedAverage { it.grade })

// Letter grade for the load
val CourseRepresentable.loadLetter: String
    get() = letter(calculateWeightedAverage { it.load })

// Letter grade for the speech
val CourseRepresentable.speechLetter: String
    get() = letter(calculateWeightedAverage { it.speech })


private fun CourseRepresentable.calculateWeightedAverage(selector: (CourseRepresentable) -> Double): Double {
    val numerator = selector(this) * (credit + creditAu)
    val denominator = credit + creditAu
    return if (denominator > 0) numerator / denominator else 0.0
}

// safely get letter grade string
private fun CourseRepresentable.letter(value: Double): String {
    val index = value.roundToInt()
    return letters.getOrElse(index) { "?" }
}