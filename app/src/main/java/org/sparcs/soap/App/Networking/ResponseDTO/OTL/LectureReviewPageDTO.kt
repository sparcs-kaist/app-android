package org.sparcs.soap.App.Networking.ResponseDTO.OTL

import com.google.gson.annotations.SerializedName
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage

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
