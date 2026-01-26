package org.sparcs.soap.App.Domain.Models.OTL

data class LectureReview(
    val id: Int,
    val lecture: Lecture,
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
