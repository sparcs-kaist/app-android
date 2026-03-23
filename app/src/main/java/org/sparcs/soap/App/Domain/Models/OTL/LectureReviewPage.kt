package org.sparcs.soap.App.Domain.Models.OTL

import kotlin.math.roundToInt

data class LectureReviewPage(
    val reviews: List<LectureReview>,
    val averageGrade: Double,
    val averageLoad: Double,
    val averageSpeech: Double,
    val department: Department?,
    val totalCount: Int
) {
    fun getGradeLetter(credits: Int): String {
        return letter((credits * averageGrade).roundToInt())
    }

    fun getLoadLetter(credits: Int): String {
        return letter((credits * averageLoad).roundToInt())
    }

    fun getSpeechLetter(credits: Int): String {
        return letter((credits * averageSpeech).roundToInt())
    }

    // safely get letter grade string
    private fun letter(index: Int): String {
        return Timetable.letters.getOrNull(index) ?: "?"
    }

    companion object
}