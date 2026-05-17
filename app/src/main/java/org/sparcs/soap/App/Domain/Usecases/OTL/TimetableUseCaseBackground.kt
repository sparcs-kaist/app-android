package org.sparcs.soap.App.Domain.Usecases.OTL

import org.sparcs.soap.App.Domain.Enums.OTL.SemesterType
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable
    suspend fun getTable(id: Int): Timetable
    suspend fun getCurrentSemester(): Semester?
}

@Singleton
class TimetableUseCaseBackground @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol
) : TimetableUseCaseBackgroundProtocol {

    override suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable {        return try {
            otlTimetableRepository.getMyTimetable(
                year = year,
                semester = semesterType
            )
        } catch (e: Exception) {
            Timetable(
                id = "-1",
                lectures = emptyList()
            )
        }
    }

    override suspend fun getTable(id: Int): Timetable {
        return try {
            otlTimetableRepository.getTimetable(id)
        } catch (e: Exception) {
            Timetable(
                id = "-1",
                lectures = emptyList()
            )
        }
    }

    override suspend fun getCurrentSemester(): Semester? {
        return try {
            otlTimetableRepository.getCurrentSemester()
        } catch (_: Exception) {
            null
        }
    }
}