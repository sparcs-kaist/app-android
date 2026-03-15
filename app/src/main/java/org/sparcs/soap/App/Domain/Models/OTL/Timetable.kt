package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.DayType
import org.sparcs.soap.App.Domain.Enums.OTL.LectureType
import kotlin.math.roundToInt

data class TimetableListItem (
    val id: Int,
    val name: String,
    val year: Int,
    val semester: Int,
    val timetableOrder: Int
)

data class Timetable(
    var lectures: List<Lecture>,
) {
    private val defaultMinMinutes = 540  // 9:00 AM
    private val defaultMaxMinutes = 1080 // 6:00 PM

    companion object {
        val letters = listOf(
            "?", "F", "F", "F", "D-", "D", "D+", "C-", "C", "C+",
            "B-", "B", "B+", "A-", "A", "A+"
        )
    }

    // Return the minimum start minutes.
    val minMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }.minOfOrNull { it.begin }
            ?.let { (it / 60) * 60 } ?: defaultMinMinutes

    // Return the maximum end minutes.
    val gappedMaxMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }.maxOfOrNull { it.end }
            ?.let { ((it / 60) + 1) * 60 } ?: defaultMaxMinutes

    val maxMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }
            .maxOfOrNull { it.end }
            ?: defaultMaxMinutes

    // Return the maximum duration of the total timetable.
    val gappedDuration: Int
        get() = gappedMaxMinutes - minMinutes

    val duration: Int
        get() = maxMinutes - minMinutes

    // Return visible days. Return all weekdays by default, and check for the need of weekends inclusion.
    val visibleDays: List<DayType>
        get() {
            val classDays = lectures.flatMap { it.classTimes.map { ct -> ct.day } }
            return (classDays + DayType.weekdays()).distinct().sorted()
        }

    // Get all lectures for day. Return LectureItem that includes index of ClassTime of the Lecture.
    fun getLectures(day: DayType, selectedLecture: Lecture?): List<LectureItem> {
        val lectureItems = lectures.flatMap { lecture ->
            lecture.classTimes.mapIndexedNotNull { index, ct ->
                if (ct.day == day) LectureItem(lecture = lecture, index = index) else null
            }
        }.toMutableList()

        selectedLecture?.let { sel ->
            val candidateBlocks = sel.classTimes.mapIndexedNotNull { index, ct ->
                if (ct.day == day) LectureItem(lecture = sel, index = index) else null
            }
            candidateBlocks.forEach { block ->
                if (lectureItems.none { it.lecture.id == sel.id && it.index == block.index }) {
                    lectureItems.add(block)
                }
            }
        }
        return lectureItems
    }

    /*
     * For Timetable Summary
     */

    // Get the sum of credits
    val credits: Int
        get() = lectures.sumOf { it.credit }

    // Get the sum of AUs
    val creditAUs: Int
        get() = lectures.sumOf { it.creditAu }

    /*
       * targetCredits: sum of credit and creditAu where reviewTotalWeight is larger than 0. It is use to calculate letter for grade, load, and speech.
       */
    val targetCredits: Int
        get() = lectures
            .sumOf { it.credit + it.creditAu }

    private fun calculateWeightedAverage(
        selector: (Lecture) -> Double,
        withCredits: Boolean = true,
    ): Double {
        val numerator =
            lectures.sumOf { selector(it) * (it.credit + if (withCredits) it.creditAu else 0) }
        val denominator = lectures.sumOf { it.credit + if (withCredits) it.creditAu else 0 }
        return if (denominator > 0) numerator / denominator else 0.0
    }

    // safely get letter grade string
    private fun letter(value: Double): String {
        val index = value.roundToInt()
        return letters.getOrElse(index) { "?" }
    }

    // Letter grade for the grade
    val gradeLetter: String
        get() = letter(calculateWeightedAverage({ it.grade }))

    // Letter grade for the load
    val loadLetter: String
        get() = letter(calculateWeightedAverage({ it.load }))

    // Letter grade for the speech
    val speechLetter: String
        get() = letter(calculateWeightedAverage({ it.speech }))

    // Get credits(credits, AUs) for the LectureType
    fun getCreditsFor(type: LectureType): Int =
        lectures.filter { LectureType.fromString(it.type) == type }.sumOf { it.credit + it.creditAu }

    fun hasCollision(newLecture: Lecture): Boolean {
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

    fun hasCollisions(a: Lecture, b: Lecture): Boolean {
        for (existingTime in b.classTimes) {
            for (newTime in a.classTimes) {
                if (existingTime.day == newTime.day) {
                    if (newTime.begin < existingTime.end && newTime.end > existingTime.begin) {
                        return true
                    }
                }
            }
        }
        return false
    }
}