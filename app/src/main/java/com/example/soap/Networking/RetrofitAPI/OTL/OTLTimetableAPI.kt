package com.example.soap.Networking.RetrofitAPI.OTL

import com.example.soap.Networking.ResponseDTO.OTL.SemesterDTO
import com.example.soap.Networking.ResponseDTO.OTL.TimetableDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLTimetableApi {

    @GET("/api/users/{userID}/timetables")
    suspend fun fetchTables(
        @Path("userID") userID: Int,
        @Query("year") year: Int,
        @Query("semester") semester: Int,
        @Query("order") order: String = "arrange_order"
    ): List<TimetableDTO>

    @POST("/api/users/{userID}/timetables")
    suspend fun createTable(
        @Path("userID") userID: Int,
        @Query("year") year: Int,
        @Query("semester") semester: Int,
        @Body lectures: List<Any> = emptyList()
    ): TimetableDTO

    @DELETE("/api/users/{userId}/timetables/{timetableId}")
    suspend fun deleteTable(
        @Path("userId") userId: Int,
        @Path("timetableId") timetableId: Int
    )

    @POST("/api/users/{userId}/timetables/{timetableId}/add-lecture")
    suspend fun addLecture(
        @Path("userId") userId: Int,
        @Path("timetableId") timetableId: Int,
        @Query("lecture")lectureID: Int
    ): TimetableDTO

    @POST("/api/users/{userId}/timetables/{timetableId}/remove-lecture")
    suspend fun deleteLecture(
        @Path("userId") userId: Int,
        @Path("timetableId") timetableId: Int,
        @Query("lecture")lectureID: Int
    ): TimetableDTO

    @GET("/api/semesters")
    suspend fun fetchSemesters(): List<SemesterDTO>

    @GET("/api/semesters/current")
    suspend fun fetchCurrentSemester(): SemesterDTO
}