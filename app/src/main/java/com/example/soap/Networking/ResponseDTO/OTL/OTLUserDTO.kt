package com.example.soap.Networking.ResponseDTO.OTL

import com.example.soap.Domain.Models.OTL.OTLUser
import kotlinx.serialization.SerialName

data class OTLUserDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("email")
    val email: String,

    @SerialName("student_id")
    val studentID: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("department")
    val department: DepartmentDTO?,

    @SerialName("majors")
    val majors: List<DepartmentDTO>,

    @SerialName("review_writable_lectures")
    val reviewWritableLectures: List<LectureDTO>,

    @SerialName("my_timetable_lectures")
    val myTimetableLectures: List<LectureDTO>,

    @SerialName("reviews")
    val reviews: List<LectureReviewDTO>
) {
    fun toModel(): OTLUser = OTLUser(
        id = id,
        email = email,
        studentID = studentID,
        firstName = firstName,
        lastName = lastName,
        department = department?.toModel(),
        majors = majors.map { it.toModel() },
        reviewWritableLectures = reviewWritableLectures.map { it.toModel() },
        myTimetableLectures = myTimetableLectures.map { it.toModel() },
        reviews = reviews.map { it.toModel() }
    )
}