package org.sparcs.soap.app.networking.responseDTO.otl

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.app.domain.models.otl.LectureReviewPage

data class LectureReviewPageDTO(
    @SerializedName("reviews")
    val reviews: List<LectureReviewDTO>,

    @SerializedName("averageGrade")
    val averageGrade: Double,

    @SerializedName("averageLoad")
    val averageLoad: Double,

    @SerializedName("averageSpeech")
    val averageSpeech: Double,

    @SerializedName("department")
    val department: DepartmentDTO?,

    @SerializedName("totalCount")
    val totalCount: Int
) {
    fun toModel(): LectureReviewPage = LectureReviewPage(
        reviews = reviews.map { it.toModel() },
        department = department?.toModel(),
        averageGrade = averageGrade,
        averageLoad = averageLoad,
        averageSpeech = averageSpeech,
        totalCount = totalCount
    )
}
