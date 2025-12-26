package org.sparcs.Domain.Models.OTL

data class OTLUser(
    val id: Int,
    val email: String,
    val studentID: String,
    val firstName: String,
    val lastName: String,
    val department: Department?,
    val majors: List<Department>,
    val reviewWritableLectures: List<Lecture>,
    val myTimetableLectures: List<Lecture>,
    val reviews: List<LectureReview>
){
    companion object
}