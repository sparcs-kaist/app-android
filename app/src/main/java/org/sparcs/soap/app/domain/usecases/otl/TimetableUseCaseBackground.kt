package org.sparcs.soap.app.domain.usecases.otl

import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable
    suspend fun getTable(id: Int): Timetable
    suspend fun getCurrentSemester(): Semester?
}

@Singleton
class TimetableUseCaseBackground @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol,
) : TimetableUseCaseBackgroundProtocol {

    override suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable {
        return try {
            otlTimetableRepository.getMyTimetable(
                year = year,
                semester = semesterType
            )
        } catch (_: Exception) {
            Timetable(
                id = "-1",
                lectures = emptyList()
            )
        }
    }

    override suspend fun getTable(id: Int): Timetable {
        return try {
            otlTimetableRepository.getTimetable(id)
        } catch (_: Exception) {
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