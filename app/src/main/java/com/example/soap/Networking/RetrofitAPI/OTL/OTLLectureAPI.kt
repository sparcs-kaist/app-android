package com.example.soap.Networking.RetrofitAPI.OTL

import com.example.soap.Networking.ResponseDTO.OTL.LectureDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface OTLLectureApi {
    @GET("/api/lectures")
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
}