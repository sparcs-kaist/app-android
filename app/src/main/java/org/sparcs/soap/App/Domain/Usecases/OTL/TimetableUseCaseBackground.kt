package org.sparcs.soap.App.Domain.Usecases.OTL

import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    suspend fun getCurrentMyTable(): Timetable
}

@Singleton
class TimetableUseCaseBackground @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol
) : TimetableUseCaseBackgroundProtocol {
    override suspend fun getCurrentMyTable(): Timetable {
        return try {
            val currentSemester = otlTimetableRepository.getCurrentSemester()

            otlTimetableRepository.getMyTimetable(
                year = currentSemester.year,
                semester = currentSemester.semesterType
            )
        } catch (e: Exception) {
            Timetable(
                id = "-myTable",
                lectures = emptyList()
            )
        }
    }
}