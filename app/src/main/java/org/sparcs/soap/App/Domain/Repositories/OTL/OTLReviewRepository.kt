package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Models.OTL.LectureHistory
import org.sparcs.soap.App.Domain.Models.OTL.Review
import org.sparcs.soap.App.Domain.Models.OTL.ReviewResponse
import org.sparcs.soap.App.Networking.RequestDTO.OTL.EditReviewRequest
import org.sparcs.soap.App.Networking.RequestDTO.OTL.WriteReviewRequest
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.LikeReviewRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLReviewApi
import org.sparcs.soap.App.Shared.Mocks.mock
import org.sparcs.soap.App.Shared.Mocks.mockList
import javax.inject.Inject

interface OTLReviewRepositoryProtocol {
    suspend fun fetchReviews(
        mode: String = "default", courseId: Int? = null, professorId: Int? = null, year: Int? = null, semester: Int? = null,
        departmentId: Int? = null, offset: Int = 0, limit: Int = 10): ReviewResponse

    suspend fun createReview(lectureId: Int, content: String, grade: Int, load: Int, speech: Int): Int

    suspend fun updateReview(reviewId: Int, content: String, grade: Int, load: Int, speech: Int)

    suspend fun likeReview(reviewId: Int, like: Boolean)

    suspend fun fetchLectureHistory(userId: Int): List<LectureHistory>

    suspend fun getWrittenReviews(): List<Review>
}

class OTLReviewRepository @Inject constructor(
    private val api: OTLReviewApi,
    private val gson: Gson = Gson()
) : OTLReviewRepositoryProtocol {

    override suspend fun fetchReviews(
        mode: String,
        courseId: Int?,
        professorId: Int?,
        year: Int?,
        semester: Int?,
        departmentId: Int?,
        offset: Int,
        limit: Int
    ): ReviewResponse {
        return safeApiCall(gson) {
            api.fetchReviews(
                mode = mode,
                courseId = courseId,
                professorId = professorId,
                year = year,
                semester = semester,
                departmentId = departmentId,
                offset = offset,
                limit = limit
            )
        }.let { response ->
            ReviewResponse(
                reviews = response.reviews.map { it.toModel() },
                grade = response.averageGrade,
                load = response.averageLoad,
                speech = response.averageSpeech
            )
        }
    }

    override suspend fun createReview(
        lectureId: Int,
        content: String,
        grade: Int,
        load: Int,
        speech: Int
    ): Int {
        return safeApiCall(gson) {
            val request = WriteReviewRequest(
                lectureID = lectureId,
                content = content,
                grade = grade,
                load = load,
                speech = speech
            )
            api.createReview(request)
        }.reviewId
    }

    override suspend fun updateReview(
        reviewId: Int,
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
            api.updateReview(reviewId, request)
        }
    }

    override suspend fun likeReview(reviewId: Int, like: Boolean) {
        safeApiCall(gson) {
            val request = LikeReviewRequest(reviewId, like.let { if (it) "like" else "unlike" })
            api.likeReview(reviewId, request)
        }
    }

    override suspend fun fetchLectureHistory(userId: Int): List<LectureHistory> {
        return safeApiCall(gson) {
            api.fetchCompletedLectures(userId)
        }.lecturesWrap.map { it.toModel() }
    }

    override suspend fun getWrittenReviews(): List<Review> {
        return safeApiCall(gson) {
            api.fetchWrittenReviews()
        }.reviews.map { it.toModel() }
    }
}

class FakeOTLReviewRepository: OTLReviewRepositoryProtocol {
    override suspend fun fetchReviews(
        mode: String, courseId: Int?, professorId: Int?, year: Int?,
        semester: Int?, departmentId: Int?, offset: Int, limit: Int
    ): ReviewResponse {
        return ReviewResponse.mock()
    }

    override suspend fun createReview(lectureId: Int, content: String, grade: Int, load: Int, speech: Int): Int {
        return 12345
    }

    override suspend fun updateReview(reviewId: Int, content: String, grade: Int, load: Int, speech: Int) {}
    override suspend fun likeReview(reviewId: Int, like: Boolean) {}

    override suspend fun fetchLectureHistory(userId: Int): List<LectureHistory> {
        TODO("Not yet implemented")
    }

    override suspend fun getWrittenReviews(): List<Review> {
        return Review.mockList()
    }
}
