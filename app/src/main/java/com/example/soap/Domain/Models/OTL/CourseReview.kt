package com.example.soap.Domain.Models.OTL

import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Helpers.LocalizedString

data class CourseReview(
    val id: Int,
    val content: String,
    val professor: LocalizedString?,
    val year: Int,
    val semester: SemesterType,
    val grade: Int,
    var like: Int,
    val load: Int,
    val speech: Int,
    val isDeleted: Boolean,
    var isLiked: Boolean
) {
    companion object {
        val letters = listOf("?", "F", "D", "C", "B", "A")
    }

    // Letter grade for the grade
    val gradeLetter: String
        get() = letters.getOrElse(grade) { "?" }

    // Letter grade for the load
    val loadLetter: String
        get() = letters.getOrElse(load) { "?" }

    // Letter grade for the speech
    val speechLetter: String
        get() = letters.getOrElse(speech) { "?" }
}