package org.sparcs.soap.App.Domain.Repositories.OTL

import com.google.gson.Gson
import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableCreation
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary
import org.sparcs.soap.App.Networking.ResponseDTO.safeApiCall
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.CreateTableRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.DeleteTableRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.LectureRequest
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.OTLTimetableApi
import org.sparcs.soap.App.Networking.RetrofitAPI.OTL.RenameTableRequest
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol {
    suspend fun getTimetables(year: Int, semester: SemesterType): List<TimetableSummary>
    suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable
    suspend fun getTimetable(timetableID: Int): Timetable
    suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation
    suspend fun deleteTable(timetableID: Int)
    suspend fun renameTable(timetableID: Int, title: String)
    suspend fun addLecture(timetableID: Int, lectureID: Int)
    suspend fun deleteLecture(timetableID: Int, lectureID: Int)
    suspend fun getSemesters(): List<Semester>
    suspend fun getCurrentSemester(): Semester
}

class OTLTimetableRepository @Inject constructor(
    private val api: OTLTimetableApi,
    private val gson: Gson = Gson(),
) : OTLTimetableRepositoryProtocol {

    override suspend fun getTimetables(year: Int, semester: SemesterType): List<TimetableSummary> = safeApiCall(gson) {
        api.fetchTimeTables(year, semester.intValue)
    }.timetables.map { it.toModel() }

    override suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable = safeApiCall(gson) {
        api.fetchMyTimetable(year, semester.intValue)
    }.toModel(id = "$year-${semester.name}-myTable")

    override suspend fun getTimetable(timetableID: Int): Timetable = safeApiCall(gson) {
        api.fetchTimeTable(timetableID)
    }.toModel(id = timetableID.toString())

    override suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation = safeApiCall(gson) {
        api.createTable(request = CreateTableRequest(year, semester.intValue))
    }

    override suspend fun deleteTable(timetableID: Int) {
        safeApiCall(gson) { api.deleteTable(DeleteTableRequest(id = timetableID)) }
    }

    override suspend fun renameTable(timetableID: Int, title: String) {
        safeApiCall(gson) {
            api.renameTable(request = RenameTableRequest(timetableID, title))
        }
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
