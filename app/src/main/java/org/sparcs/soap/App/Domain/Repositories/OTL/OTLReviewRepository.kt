package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.LectureHistory
import org.sparcs.soap.App.Domain.Models.OTL.LectureReview
import org.sparcs.soap.App.Domain.Models.OTL.LectureReviewPage
import org.sparcs.soap.App.Networking.RequestDTO.OTL.EditReviewRequest
import org.sparcs.soap.App.Networking.RequestDTO.OTL.WriteReviewRequest
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.LikeReviewRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLReviewApi
import javax.inject.Inject

interface OTLReviewRepositoryProtocol {
    suspend fun fetchReviews(
        mode: String = "default", courseID: Int? = null, professorID: Int? = null, year: Int? = null, semester: Int? = null,
        departmentId: Int? = null, offset: Int = 0, limit: Int = 10): LectureReviewPage

    suspend fun writeReview(lectureID: Int, content: String, grade: Int, load: Int, speech: Int): Int

    suspend fun updateReview(reviewID: Int, content: String, grade: Int, load: Int, speech: Int)

    suspend fun likeReview(reviewID: Int, like: Boolean)

    suspend fun fetchLectureHistory(userID: Int): List<LectureHistory>

    suspend fun getWrittenReviews(): List<LectureReview>
}

class OTLReviewRepository @Inject constructor(
    private val api: OTLReviewApi,
    private val gson: Gson = Gson()
) : OTLReviewRepositoryProtocol {

    override suspend fun fetchReviews(
        mode: String,
        courseID: Int?,
        professorID: Int?,
        year: Int?,
        semester: Int?,
        departmentId: Int?,
        offset: Int,
        limit: Int
    ): LectureReviewPage {
        return safeApiCall(gson) {
            api.fetchReviews(
                mode = mode,
                courseID = courseID,
                professorID = professorID,
                year = year,
                semester = semester,
                departmentID = departmentId,
                offset = offset,
                limit = limit
            )
        }.toModel()
    }

    override suspend fun writeReview(
        lectureID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ): Int {
        return safeApiCall(gson) {
            val request = WriteReviewRequest(
                lectureID = lectureID,
                content = content,
                grade = grade,
                load = load,
                speech = speech
            )
            api.writeReview(request)
        }.reviewId
    }

    override suspend fun updateReview(
        reviewID: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ) {
        safeApiCall(gson) {
            val request = EditReviewRequest(
                content = content,
                grade = grade,
                load = load,
                speech = speech
            )
            api.updateReview(reviewID, request)
        }
    }

    override suspend fun likeReview(reviewID: Int, like: Boolean) {
        safeApiCall(gson) {
            val request = LikeReviewRequest(reviewID, like.let { if (it) "like" else "unlike" })
            api.toggleLikeReview(reviewID, request)
        }
    }

    override suspend fun fetchLectureHistory(userID: Int): List<LectureHistory> {
        return safeApiCall(gson) {
            api.fetchCompletedLectures(userID)
        }.lecturesWrap.map { it.toModel() }
    }

    override suspend fun getWrittenReviews(): List<LectureReview> {
        return safeApiCall(gson) {
            api.fetchWrittenReviews()
        }.reviews.map { it.toModel() }
    }
}
