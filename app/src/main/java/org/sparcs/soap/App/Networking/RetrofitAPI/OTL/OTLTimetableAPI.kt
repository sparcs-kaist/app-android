package org.sparcs.soap.App.Networking.RetrofitAPI.OTL

import org.sparcs.soap.App.Domain.Models.OTL.TimetableCreation
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.SemesterDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.SemesterListDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.TimetableDTO
import org.sparcs.soap.App.Networking.ResponseDTO.OTL.TimetableSummaryListDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OTLTimetableApi {
    @GET("api/v2/timetables")
    suspend fun fetchTimeTables(
        @Query("year") year: Int,
        @Query("semester") semester: Int,
    ): TimetableSummaryListDTO

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
    ): TimetableCreation

    @HTTP(method = "DELETE", path = "api/v2/timetables", hasBody = true)
    suspend fun deleteTable(
        @Body request: DeleteTableRequest
    )

    @PATCH("/api/v2/timetables")
    suspend fun renameTable(
        @Body request: RenameTableRequest
    ): TimetableDTO

    @PATCH("api/v2/timetables/{timetableId}")
    suspend fun patchLecture(
        @Path("timetableId") timetableId: Int,
        @Body request: LectureRequest
    )

    @GET("api/v2/semesters")
    suspend fun fetchSemesters(): SemesterListDTO

    @GET("api/v2/semesters/current")
    suspend fun fetchCurrentSemester(): SemesterDTO
}

data class CreateTableRequest(
    val year: Int,
    val semester: Int,
    val lectureIds: List<Int> = emptyList()
)

data class DeleteTableRequest(
    val id: Int
)

data class RenameTableRequest(
    val id: Int,
    val name: String
)

data class LectureRequest(
    val lectureId: Int,
    val action: String // "add" or "delete"
)