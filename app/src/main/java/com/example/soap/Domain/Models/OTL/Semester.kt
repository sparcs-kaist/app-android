package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Enums.SemesterType
import java.util.Date

data class Semester(
    val year: Int,
    val semesterType: SemesterType,
    val beginDate: Date,
    val endDate: Date,
    val eventDate: SemesterEventDate
) : Comparable<Semester> {

    val id: String
        get() = "$year-${semesterType}"

    val description: String
        get() = "$year ${semesterType.rawValue}"

    override fun compareTo(other: Semester): Int {
        return if (this.year != other.year) {
            this.year.compareTo(other.year)
        } else {
            this.semesterType.compareTo(other.semesterType)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Semester) return false

        return this.year == other.year && this.semesterType == other.semesterType
    }

    override fun hashCode(): Int {
        var result = year
        result = 31 * result + semesterType.hashCode()
        return result
    }

    companion object{}
}


data class SemesterEventDate(
    val registrationPeriodStartDate: Date?,
    val registrationPeriodEndDate: Date?,
    val addDropPeriodEndDate: Date?,
    val dropDeadlineDate: Date?,
    val evaluationDeadlineDate: Date?,
    val gradePostingDate: Date?
)