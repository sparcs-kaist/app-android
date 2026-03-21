package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.ResponseDTO.OTL.CourseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.CoursePageDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLCourseApi {
    @GET("api/v2/courses")
    suspend fun searchCourse(
        @Query("keyword") name: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): CoursePageDTO

    @GET("api/v2/courses/{courseId}")
    suspend fun fetchCourse(
        @Path("courseId") courseId: Int
    ): CourseDTO
}