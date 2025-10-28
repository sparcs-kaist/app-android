package com.example.soap.Domain.Repositories.OTL

import com.example.soap.Domain.Enums.SemesterType
import com.example.soap.Domain.Models.OTL.Semester
import com.example.soap.Domain.Models.OTL.Timetable
import com.example.soap.Networking.RetrofitAPI.OTL.AddLectureRequest
import com.example.soap.Networking.RetrofitAPI.OTL.CreateTableRequest
import com.example.soap.Networking.RetrofitAPI.OTL.OTLTimetableApi
import javax.inject.Inject

interface OTLTimetableRepositoryProtocol{
    suspend fun getTables(userID: Int, year: Int, semester: SemesterType): List<Timetable>
    suspend fun createTable(userID: Int, year: Int, semester: SemesterType): Timetable
    suspend fun deleteTable(userID: Int, timetableID: Int)
    suspend fun addLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable
    suspend fun deleteLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable
    suspend fun getSemesters(): List<Semester>
    suspend fun getCurrentSemester(): Semester
}

class OTLTimetableRepository @Inject constructor(
    private val api: OTLTimetableApi
) : OTLTimetableRepositoryProtocol {

    override suspend fun getTables(userID: Int, year: Int, semester: SemesterType): List<Timetable> {
        return api.fetchTables(userID, year, semester.intValue).map { it.toModel() }
    }

    override suspend fun createTable(userID: Int, year: Int, semester: SemesterType): Timetable {
        val request = CreateTableRequest(
            year = year,
            semester = semester.intValue,
            lectures = emptyList()
        )
        return api.createTable(userID, request).toModel()
    }

    override suspend fun deleteTable(userID: Int, timetableID: Int) {
        api.deleteTable(userID, timetableID)
    }

    override suspend fun addLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable {
        return api.addLecture(userID, timetableID, request = AddLectureRequest(lecture = lectureID)).toModel()
    }

    override suspend fun deleteLecture(userID: Int, timetableID: Int, lectureID: Int): Timetable {
        return api.deleteLecture(userID, timetableID, lectureID).toModel()
    }

    override suspend fun getSemesters(): List<Semester> {
        return api.fetchSemesters().map { it.toModel() }
    }

    override suspend fun getCurrentSemester(): Semester {
        return api.fetchCurrentSemester().toModel()
    }
}