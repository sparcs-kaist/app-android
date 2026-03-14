package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Helpers.CourseRepresentable

data class Review(
    val id: Int,
    val courseId: Int,
    val lectureId: Int,
    val courseName: String,
    val professors: List<Professor>,
    val year: Int,
    val semester: SemesterType,
    val content: String,
    var like: Int,
    val grade: Int,
    val load: Int,
    val speech: Int,
    var isDeleted: Boolean,
    var isLiked: Boolean
) {

    companion object {
        val letters = listOf("?", "F", "D", "C", "B", "A")
    }

    // Letter grade for the grade
    val gradeLetter: String
        get() = if (grade in letters.indices) letters[grade] else "?"

    // Letter grade for the load
    val loadLetter: String
        get() = if (load in letters.indices) letters[load] else "?"

    // Letter grade for the speech
    val speechLetter: String
        get() = if (speech in letters.indices) letters[speech] else "?"
}

data class ReviewResponse(
    val reviews: List<Review>,
    override val grade: Double,
    override val load: Double,
    override val speech: Double
): CourseRepresentable {
    companion object
}
