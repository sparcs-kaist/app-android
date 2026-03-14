package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableListItem
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.CreateTableRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.LectureRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLTimetableApi
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol {
    suspend fun getTimetables(year: Int, semester: SemesterType): List<TimetableListItem>
    suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable
    suspend fun getTimetable(timetableID: Int): Timetable
    suspend fun createTable(year: Int, semester: SemesterType): Int
    suspend fun deleteTable(timetableID: Int)
    suspend fun addLecture(timetableID: Int, lectureID: Int)
    suspend fun deleteLecture(timetableID: Int, lectureID: Int)
    suspend fun getSemesters(): List<Semester>
    suspend fun getCurrentSemester(): Semester
}

class OTLTimetableRepository @Inject constructor(
    private val api: OTLTimetableApi,
    private val gson: Gson = Gson(),
) : OTLTimetableRepositoryProtocol {

    override suspend fun getTimetables(year: Int, semester: SemesterType, ): List<TimetableListItem> = safeApiCall(gson) {
        api.fetchTimeTables(year, semester.intValue)
    }.timetables.map { it.toModel() }

    override suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable = safeApiCall(gson) {
        api.fetchMyTimetable(year, semester.intValue)
    }.toModel()

    override suspend fun getTimetable(timetableID: Int): Timetable = safeApiCall(gson) {
        api.fetchTimeTable(timetableID)
    }.toModel()

    override suspend fun createTable(year: Int, semester: SemesterType): Int = safeApiCall(gson) {
        api.createTable(request = CreateTableRequest(year, semester.intValue)).id
    }

    override suspend fun deleteTable(timetableID: Int) = safeApiCall(gson) {
        api.deleteTable(timetableID)
    }

    override suspend fun addLecture(timetableID: Int, lectureID: Int) = safeApiCall(gson) {
        api.patchLecture(timetableID, request = LectureRequest(lectureId = lectureID, action = "add"))
    }

    override suspend fun deleteLecture(timetableID: Int, lectureID: Int) {
        safeApiCall(gson) {
            api.patchLecture(timetableID, request = LectureRequest(lectureId = lectureID, action = "delete"))
        }
    }

    override suspend fun getSemesters(): List<Semester> = safeApiCall(gson) {
        api.fetchSemesters()
    }.semesters.map { it.toModel() }

    override suspend fun getCurrentSemester(): Semester = safeApiCall(gson) {
        api.fetchCurrentSemester()
    }.toModel()
}
