package com.example.soap.Networking.RetrofitAPI.OTL

import com.example.soap.Networking.RequestDTO.OTL.WriteReviewRequest
import com.example.soap.Networking.ResponseDTO.OTL.LectureDTO
import com.example.soap.Networking.ResponseDTO.OTL.LectureReviewDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLLectureApi {
    @GET("api/lectures")
    suspend fun searchLecture(
        @Query("year") year: Int,
        @Query("semester") semester: Int,
        @Query("keyword") keyword: String,
        @Query("type") type: String,
        @Query("department") department: String,
        @Query("level") level: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): List<LectureDTO>


    @GET("api/lectures/{lectureID}/related-reviews")
    suspend fun fetchReviews(
        @Path("lectureID") lectureID: Int,
        @Query("order") order: String = "-written_datetime",
        @Query("limit") limit: Int = 100
    ): List<LectureReviewDTO>

    @POST("api/reviews/{lectureID}")
    suspend fun writeReview(
        @Path("lectureID") lectureID: Int,
        @Body request: WriteReviewRequest
    ): LectureReviewDTO
}