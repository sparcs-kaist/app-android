package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Enums.DayType
import com.example.soap.Domain.Enums.LectureType
import kotlin.math.roundToInt

data class Timetable(
    val id: String,
    var lectures: List<Lecture>
) {
    private val defaultMinMinutes = 540  // 9:00 AM
    private val defaultMaxMinutes = 1080 // 6:00 PM

    companion object {
        val letters = listOf(
            "?", "F", "F", "F", "D-", "D", "D+", "C-", "C", "C+",
            "B-", "B", "B+", "A-", "A", "A+"
        )
    }

    val minMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }.minOfOrNull { it.begin }
            ?.let { (it / 60) * 60 } ?: defaultMinMinutes

    val maxMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }.maxOfOrNull { it.end }
            ?.let { ((it / 60) + 1) * 60 } ?: defaultMaxMinutes

    val duration: Int
        get() = maxMinutes - minMinutes

    val visibleDays: List<DayType>
        get() {
            val classDays = lectures.flatMap { it.classTimes.map { ct -> ct.day } }
            return (classDays + DayType.weekdays()).distinct().sorted()
        }

    fun getLectures(day: DayType): List<LectureItem> =
        lectures.flatMap { lecture ->
            lecture.classTimes.mapIndexedNotNull { index, ct ->
                if (ct.day == day) LectureItem(lecture = lecture, index = index) else null
            }
        }

    val credits: Int
        get() = lectures.sumOf { it.credit }

    val creditAUs: Int
        get() = lectures.sumOf { it.creditAu }

    val targetCredits: Int
        get() = lectures
            .filter { it.reviewTotalWeight > 0.0 }
            .sumOf { it.credit + it.creditAu }

    private fun calculateWeightedAverage(
        selector: (Lecture) -> Double,
        withCredits: Boolean = true
    ): Double {
        val relevant = lectures.filter { it.reviewTotalWeight > 0.0 }
        val numerator = relevant.sumOf { selector(it) * (it.credit + if (withCredits) it.creditAu else 0) }
        val denominator = relevant.sumOf { it.credit + if (withCredits) it.creditAu else 0 }
        return if (denominator > 0) numerator / denominator else 0.0
    }

    private fun letter(value: Double): String {
        val index = value.roundToInt()
        return letters.getOrElse(index) { "?" }
    }

    val gradeLetter: String
        get() = letter(calculateWeightedAverage ({ it.grade }))

    val loadLetter: String
        get() = letter(calculateWeightedAverage ({ it.load }))

    val speechLetter: String
        get() = letter(calculateWeightedAverage ({ it.speech }))

    fun getCreditsFor(type: LectureType): Int =
        lectures.filter { it.type == type }.sumOf { it.credit + it.creditAu }

    fun hasCollision(newLecture: Lecture, lectures: List<Lecture>): Boolean {
        for (existingLecture in lectures) {
            for (existingTime in existingLecture.classTimes) {
                for (newTime in newLecture.classTimes) {
                    if (existingTime.day == newTime.day) {
                        // Overlap occurs if start < other.end && end > other.start
                        if (newTime.begin < existingTime.end && newTime.end > existingTime.begin) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

}