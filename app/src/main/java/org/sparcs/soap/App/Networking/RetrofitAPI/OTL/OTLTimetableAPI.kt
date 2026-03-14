package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Networking.ResponseDTO.OTL.SemesterDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.SemesterResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.TimetableDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.TimetableListDTO
import retrofit2.http.*

interface OTLTimetableApi {
    @GET("api/v2/timetables")
    suspend fun fetchTimeTables(
        @Query("year") year: Int,
        @Query("semester") semester: Int,
    ): TimetableListDTO

    @GET("api/v2/timetables/my-timetable")
    suspend fun fetchMyTimetable(
        @Query("year") year: Int,
        @Query("semester") semester: Int
    ): TimetableDTO

    @GET("api/v2/timetables/{timetableId}")
    suspend fun fetchTimeTable(
        @Path("timetableId") timetableId: Int
    ): TimetableDTO

    @POST("api/v2/timetables")
    suspend fun createTable(
        @Body request: CreateTableRequest
    ): CreateTimetableResponse

    @DELETE("api/v2/timetables")
    suspend fun deleteTable(
        @Path("id") timetableId: Int
    )

    @PATCH("api/v2/timetables/{timetableId}")
    suspend fun patchLecture(
        @Path("timetableId") timetableId: Int,
        @Body request: LectureRequest
    )

    @GET("api/v2/semesters")
    suspend fun fetchSemesters(): SemesterResponseDTO

    @GET("api/v2/semesters/current")
    suspend fun fetchCurrentSemester(): SemesterDTO
}

data class CreateTableRequest(
    val year: Int,
    val semester: Int,
    val lectureIds: List<Int> = emptyList()
)

data class LectureRequest(
    val lectureId: Int,
    val action: String // "add" or "delete"
)

data class CreateTimetableResponse(
    val id: Int
)