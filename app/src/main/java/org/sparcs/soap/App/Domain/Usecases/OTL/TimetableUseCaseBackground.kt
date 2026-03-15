package org.sparcs.soap.App.Domain.Usecases.OTL

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    val currentSemester: Semester?
    suspend fun load()
    suspend fun getMyTable(semester: Semester): Timetable
}

@Singleton
class TimetableUseCaseBackground @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol
) : TimetableUseCaseBackgroundProtocol {

    override var currentSemester: Semester? = null

    private val loadMutex = Mutex()

    override suspend fun load() {
        loadMutex.withLock {
            try {
                this.currentSemester = otlTimetableRepository.getCurrentSemester()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getMyTable(semester: Semester): Timetable {
        return Timetable(lectures = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType).lectures)
    }
}