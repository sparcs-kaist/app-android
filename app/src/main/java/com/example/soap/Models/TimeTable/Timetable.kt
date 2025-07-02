package com.example.soap.Models.TimeTable

import com.example.soap.Models.Types.DayType
import com.example.soap.Models.Types.LectureType


data class Timetable(
    val id: Int,
    var lectures: List<Lecture>,
    val semester: Semester
) : Comparable<Timetable> {


    companion object {
        val defaultMinMinutes = 540 //9:00AM
        val defaultMaxMinutes = 1080 // 6:00 PM


        val letters: List<String> = listOf(
            "?", "F", "F", "F", "D-", "D", "D+", "C-", "C", "C+",
            "B-", "B", "B+", "A-", "A", "A+"
        )
    }

    // Comparable
    override fun compareTo(other: Timetable): Int {
        return if (this.semester == other.semester) {
            this.id.compareTo(other.id)
        } else {
            this.semester.compareTo(other.semester)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timetable

        if (id != other.id) return false
        if (semester != other.semester) return false

        return true
    }
}
    // Return the minimum end minutes.
    val Timetable.minMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }
            .map { it.begin }
            .minOrNull()
            ?.let { (it / 60) * 60 } ?: Timetable.defaultMinMinutes

    // Return the maximum end minutes.
    val Timetable.maxMinutes: Int
        get() = lectures
            .flatMap { it.classTimes }.maxOfOrNull { it.end }
            ?.let { ((it / 60) + 1) * 60 } ?: Timetable.defaultMaxMinutes

    // Return the maximum duration of the total timetable.
    val Timetable.duration: Int
        get() = maxMinutes - minMinutes

    // Return visible days. Return all weekdays by default, and check for the need of weekends inclusion.
    val Timetable.visibleDays: List<DayType>
        get() {
            val classDays = lectures.flatMap { it.classTimes.map { time -> time.day } }
            val examDays = lectures.flatMap { it.examTimes.map { time -> time.day } }

            val defaultWeekdays =
                listOf(DayType.MON, DayType.TUE, DayType.WED, DayType.THU, DayType.FRI)
            val allDays = (classDays + examDays + defaultWeekdays).toSet()

            return allDays.sorted()
        }

    // Get all lectures for day. Return LectureItem that includes index of ClassTime of the Lecture.
    fun Timetable.getLectures(day: DayType): List<LectureItem> {
        return lectures
            .flatMap { lecture ->
                lecture.classTimes.withIndex()
                    .filter { indexedValue -> indexedValue.value.day == day }
                    .map { indexedValue ->
                        LectureItem(
                            lecture = lecture,
                            index = indexedValue.index
                        )
                    }
            }
    }

    /*
    * For Timetable Summary
    */


    // Get the sum of credits
    val Timetable.credits: Int
        get() = lectures.sumOf { it.credit }

    // Get the sum of AUs
    val Timetable.creditAUs: Int
        get() = lectures.sumOf { it.creditAu }

    /*
    * targetCredits: sum of credit and creditAu where reviewTotalWeight is larger than 0. It is use to calculate letter for grade, load, and speech.
    */
    val Timetable.targetCredits: Int
        get() = lectures
            .filter { it.reviewTotalWeight > 0 }
            .sumOf { it.credit + it.creditAu }

    // Get credits(credits, AUs) for the LectureType
    fun Timetable.getCreditsFor(type: LectureType): Int {
        return lectures
            .filter { it.type == type }
            .sumOf { it.credit + it.creditAu }

}






