package org.sparcs.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.App.Domain.Models.OTL.OTLUser

data class OTLUserDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("student_id")
    val studentID: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("department")
    val department: DepartmentDTO?,

    @SerializedName("majors")
    val majors: List<DepartmentDTO>,

    @SerializedName("review_writable_lectures")
    val reviewWritableLectures: List<LectureDTO>,

    @SerializedName("my_timetable_lectures")
    val myTimetableLectures: List<LectureDTO>,

    @SerializedName("reviews")
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