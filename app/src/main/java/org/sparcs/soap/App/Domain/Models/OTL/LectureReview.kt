package org.sparcs.soap.App.Domain.Models.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType

data class LectureReview(
    val id: Int,
    val courseID: Int,
    val lectureID: Int,
    val courseName: String,
    val professors: List<Professor>,
    val year: Int,
    val semester: SemesterType,
    val content: String,
    var like: Int,
    val grade: String,
    val load: String,
    val speech: String,
    var isDeleted: Boolean,
    var likedByUser: Boolean
){
    companion object
}