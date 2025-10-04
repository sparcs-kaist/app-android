package com.example.soap.Domain.Models.OTL

data class LectureReview(
    val id: Int,
    val course: Course,
    val lecture: Lecture,
    val content: String,
    val like: Int,
    val grade: Int,
    val load: Int,
    val speech: Int,
    val isLiked: Boolean
)