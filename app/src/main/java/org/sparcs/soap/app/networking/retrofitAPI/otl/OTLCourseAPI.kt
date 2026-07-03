package org.sparcs.soap.app.networking.retrofitAPI.otl

import org.sparcs.soap.app.networking.responseDTO.otl.CourseDTO
import org.sparcs.soap.app.networking.responseDTO.otl.CoursePageDTO
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