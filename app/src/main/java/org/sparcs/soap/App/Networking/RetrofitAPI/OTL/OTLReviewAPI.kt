package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.RequestDTO.OTL.EditReviewRequest
import org.sparcs.soap.App.Networking.RequestDTO.OTL.WriteReviewRequest
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.LectureHistoryDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.LectureReviewPageDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.ReviewCreateResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.WrittenReviewResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLReviewApi {
    @GET("api/v2/reviews")
    suspend fun fetchReviews(
        @Query("mode") mode: String,
        @Query("courseId") courseID: Int?,
        @Query("professorId") professorID: Int?,
        @Query("year") year: Int?,
        @Query("semester") semester: Int?,
        @Query("department") departmentID: Int?,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): LectureReviewPageDTO

    @POST("/api/v2/reviews")
    suspend fun writeReview(
        @Body request: WriteReviewRequest
    ): ReviewCreateResponseDTO

    @PUT("api/v2/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewID: Int,
        @Body request: EditReviewRequest
    )

    @PATCH("api/v2/reviews/{reviewId}/liked")
    suspend fun toggleLikeReview(
        @Path("reviewId") reviewID: Int,
        @Body request: LikeReviewRequest
    )

    @GET("api/v2/users/{userId}/lectures")
    suspend fun fetchCompletedLectures(
        @Path("userId") userID: Int
    ): LectureHistoryDTO

    @GET("api/v2/users/written-reviews")
    suspend fun fetchWrittenReviews(): WrittenReviewResponseDTO
}

data class LikeReviewRequest (
    val reviewId: Int,
    val action: String // "like" or "unlike"
)