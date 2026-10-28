package org.sparcs.soap.app.domain.repositories.otl

import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.otl.ActivityConflictException
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableItemChange
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.networking.requestDTO.otl.TimetableItemsRequestDTO
import org.sparcs.soap.app.networking.responseDTO.safeApiCall
import org.sparcs.soap.app.networking.retrofitAPI.otl.CreateTableRequest
import org.sparcs.soap.app.networking.retrofitAPI.otl.DeleteTableRequest
import org.sparcs.soap.app.networking.retrofitAPI.otl.OTLTimetableApi
import org.sparcs.soap.app.networking.retrofitAPI.otl.RenameTableRequest
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol {
    suspend fun updateItems(timetableID: Int, changes: List<TimetableItemChange>): List<Int>
    suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft) {
        updateItems(
            timetableID,
            listOf(if (activityID == null) TimetableItemChange.AddActivity(draft) else TimetableItemChange.UpdateActivity(activityID, draft)),
        )
    }
    suspend fun deleteActivity(timetableID: Int, activityID: Int) {
        updateItems(timetableID, listOf(TimetableItemChange.RemoveActivity(activityID)))
    }

    suspend fun getTimetables(year: Int, semester: SemesterType): List<TimetableSummary>
    suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable
    suspend fun getTimetable(timetableID: Int): Timetable
    suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation
    suspend fun deleteTable(timetableID: Int)
    suspend fun renameTable(timetableID: Int, title: String)
    suspend fun addLecture(timetableID: Int, lectureID: Int) {
        updateItems(timetableID, listOf(TimetableItemChange.AddLecture(lectureID)))
    }
    suspend fun deleteLecture(timetableID: Int, lectureID: Int) {
        updateItems(timetableID, listOf(TimetableItemChange.RemoveLecture(lectureID)))
    }
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
        coroutineScope {
            val table = async { api.fetchTimeTable(timetableID) }
            val activities = async { api.fetchActivities(timetableID) }
            table.await().toModel(id = timetableID.toString()).copy(customBlocks = activities.await().custom_blocks)
        }
    }

    override suspend fun updateItems(timetableID: Int, changes: List<TimetableItemChange>): List<Int> = try {
        safeApiCall(gson) { api.updateItems(timetableID, TimetableItemsRequestDTO.of(changes)) }
            .results.sortedBy { it.index }.map { it.id }
    } catch (e: NetworkError.ServerError) {
        if (e.code == 409) throw ActivityConflictException()
        throw e
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

    override suspend fun getSemesters(): List<Semester> = safeApiCall(gson) {
        api.fetchSemesters()
    }.semesters.map { it.toModel() }

    override suspend fun getCurrentSemester(): Semester = safeApiCall(gson) {
        api.fetchCurrentSemester()
    }.toModel()
}
