package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.ResponseDTO.OTL.CourseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.LectureReviewDTO
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLCourseApi {
    @GET("api/courses")
    suspend fun searchCourse(
        @Query("keyword") name: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): List<CourseDTO>

    @GET("api/courses/{courseId}/reviews")
    suspend fun fetchReviews(
        @Path("courseId") courseId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): List<LectureReviewDTO>

    @POST("api/reviews/{reviewId}/like")
    suspend fun likeReview(@Path("reviewId") reviewId: Int)

    @DELETE("api/reviews/{reviewId}/like")
    suspend fun unlikeReview(@Path("reviewId") reviewId: Int)
}