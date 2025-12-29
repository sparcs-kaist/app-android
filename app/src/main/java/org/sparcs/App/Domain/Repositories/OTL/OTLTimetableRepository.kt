package org.sparcs.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.App.Domain.Enums.OTL.SemesterType
import org.sparcs.App.Domain.Models.OTL.Semester
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Networking.ResponseDTO.handleApiError
import org.sparcs.App.Networking.RetrofitAPI.OTL.CreateTableRequest
import org.sparcs.App.Networking.RetrofitAPI.OTL.LectureRequest
import org.sparcs.App.Networking.RetrofitAPI.OTL.OTLTimetableApi
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol {
    suspend fun getTables(userID: Int, year: Int, semester: SemesterType): List<Timetable>
    suspend fun createTable(userID: Int, year: Int, semester: SemesterType): Timetable
    suspend fun deleteTable(userID: Int, timetableID: Int)
    suspend fun addLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable
    suspend fun deleteLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable
    suspend fun getSemesters(): List<Semester>
    suspend fun getCurrentSemester(): Semester
}

class OTLTimetableRepository @Inject constructor(
    private val api: OTLTimetableApi,
    private val gson: Gson = Gson(),
) : OTLTimetableRepositoryProtocol {

    override suspend fun getTables(
        userID: Int,
        year: Int,
        semester: SemesterType,
    ): List<Timetable> = try {
        api.fetchTables(userID, year, semester.intValue).map { it.toModel() }
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun createTable(userID: Int, year: Int, semester: SemesterType): Timetable {
        try {
            val request = CreateTableRequest(
                year = year,
                semester = semester.intValue,
                lectures = emptyList()
            )
            return api.createTable(userID, request).toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }
    }

    override suspend fun deleteTable(userID: Int, timetableID: Int) = try {
        api.deleteTable(userID, timetableID)
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun addLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable =
        try {
            api.addLecture(userID, timetableID, request = LectureRequest(lecture = lectureID))
                .toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }

    override suspend fun deleteLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable =
        try {
            api.deleteLecture(userID, timetableID, request = LectureRequest(lecture = lectureID))
                .toModel()
        } catch (e: Exception) {
            handleApiError(gson, e)
        }

    override suspend fun getSemesters(): List<Semester> = try {
        api.fetchSemesters().map { it.toModel() }
    } catch (e: Exception) {
        handleApiError(gson, e)
    }

    override suspend fun getCurrentSemester(): Semester = try {
        api.fetchCurrentSemester().toModel()
    } catch (e: Exception) {
        handleApiError(gson, e)
    }
}