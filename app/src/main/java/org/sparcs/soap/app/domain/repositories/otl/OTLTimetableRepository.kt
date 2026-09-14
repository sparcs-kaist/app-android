package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.CreateTableRequest
import org.sparcs.soap.app.networking.retrofitAPI.otl.DeleteTableRequest
import org.sparcs.soap.app.networking.retrofitAPI.otl.LectureRequest
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLTimetableApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.RenameTableRequest
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol {
    suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft)
    suspend fun deleteActivity(timetableID: Int, activityID: Int)

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
        val table = api.fetchTimeTable(timetableID).toModel(id = timetableID.toString())
        table.copy(customBlocks = api.fetchActivities(timetableID).custom_blocks)
    }

    override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft) = safeApiCall(gson) {
        if (activityID == null) api.createActivity(timetableID, draft)
        else api.updateActivity(timetableID, activityID, draft)
    }

    override suspend fun deleteActivity(timetableID: Int, activityID: Int) = safeApiCall(gson) {
        api.deleteActivity(timetableID, activityID)
    }

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
