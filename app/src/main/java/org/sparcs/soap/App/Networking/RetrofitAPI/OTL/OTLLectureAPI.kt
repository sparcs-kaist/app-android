package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.ResponseDTO.OTL.LectureSearchResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface OTLLectureApi {
    @GET("api/v2/lectures")
    suspend fun searchLecture(
        @Query("year") year: Int,
        @Query("semester") semester: Int,
        @Query("keyword") keyword: String,
        @Query("type") type: List<String>?,
        @Query("department") department: List<Int>?,
        @Query("level") level: List<Int>?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): LectureSearchResponseDTO
}