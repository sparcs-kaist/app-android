package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.CreateTableRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.LectureRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLTimetableApi
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
    ): List<Timetable> = safeApiCall(gson) {
        api.fetchTables(userID, year, semester.intValue)
    }.map { it.toModel() }

    override suspend fun createTable(userID: Int, year: Int, semester: SemesterType): Timetable = safeApiCall(gson) {
        val request = CreateTableRequest(
            year = year,
            semester = semester.intValue,
            lectures = emptyList()
        )
        api.createTable(userID, request)
    }.toModel()

    override suspend fun deleteTable(userID: Int, timetableID: Int) = safeApiCall(gson) {
        api.deleteTable(userID, timetableID)
    }

    override suspend fun addLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable = safeApiCall(gson) {
        api.addLecture(userID, timetableID, request = LectureRequest(lecture = lectureID))
    }.toModel()

    override suspend fun deleteLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable = safeApiCall(gson) {
        api.deleteLecture(userID, timetableID, request = LectureRequest(lecture = lectureID))
    }.toModel()

    override suspend fun getSemesters(): List<Semester> = safeApiCall(gson) {
        api.fetchSemesters()
    }.map { it.toModel() }

    override suspend fun getCurrentSemester(): Semester = safeApiCall(gson) {
        api.fetchCurrentSemester()
    }.toModel()
}