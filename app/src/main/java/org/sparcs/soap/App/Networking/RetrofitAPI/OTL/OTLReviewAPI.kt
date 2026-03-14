package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.RequestDTO.OTL.EditReviewRequest
import org.sparcs.soap.App.Networking.RequestDTO.OTL.WriteReviewRequest
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.LectureHistoryDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.ReviewCreateResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.ReviewResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.WrittenReviewResponseDTO
import retrofit2.http.*

interface OTLReviewApi {
    @GET("api/v2/reviews")
    suspend fun fetchReviews(
        @Query("mode") mode: String,
        @Query("courseId") courseId: Int?,
        @Query("professorId") professorId: Int?,
        @Query("year") year: Int?,
        @Query("semester") semester: Int?,
        @Query("department") departmentId: Int?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): ReviewResponseDTO

    @POST("/api/v2/reviews")
    suspend fun createReview(
        @Body request: WriteReviewRequest
    ): ReviewCreateResponseDTO

    @PUT("api/v2/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Int,
        @Body request: EditReviewRequest
    )

    @PATCH("api/v2/reviews/{reviewId}/liked")
    suspend fun likeReview(
        @Path("reviewId") reviewId: Int,
        @Body request: LikeReviewRequest
    )

    @GET("api/v2/users/{userId}/lectures")
    suspend fun fetchCompletedLectures(
        @Path("userId") userId: Int
    ): LectureHistoryDTO

    @GET("api/v2/users/written-reviews")
    suspend fun fetchWrittenReviews(): WrittenReviewResponseDTO
}

data class LikeReviewRequest (
    val reviewId: Int,
    val action: String // "like" or "unlike"
)