package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.CourseHistory

data class CourseHistoryDTO(
    @SerializedName("year")
    val year: Int,

    @SerializedName("semester")
    val semester: Int,

    @SerializedName("classes")
    val classes: List<CourseHistoryClassDTO>,

    @SerializedName("myLectureId")
    val myLectureID: Int?
) {
    fun toModel(): CourseHistory {
        return CourseHistory(
            year = year,
            semester = SemesterType.fromRawValue(semester),
            classes = classes.map { it.toModel() },
            myLectureID = myLectureID
        )
    }
}
