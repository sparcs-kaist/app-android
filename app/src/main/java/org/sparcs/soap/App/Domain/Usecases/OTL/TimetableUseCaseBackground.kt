package org.sparcs.soap.App.Domain.Usecases.OTL

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    val semesters: List<Semester>
    val currentSemester: Semester?
    suspend fun load()
    fun getMyTable(semesterId: String): Timetable
}

@Singleton
class TimetableUseCaseBackground @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol
) : TimetableUseCaseBackgroundProtocol {

    private var store: Map<String, List<Timetable>> = emptyMap()
    override var semesters: List<Semester> = emptyList()
    override var currentSemester: Semester? = null

    private val loadMutex = Mutex()

    override suspend fun load() {
        if (store.isNotEmpty() || semesters.isNotEmpty()) return

        loadMutex.withLock {
            if (store.isNotEmpty() || semesters.isNotEmpty()) return

            try {
                val fetchedSemesters = otlTimetableRepository.getSemesters()
                val fetchedCurrent = otlTimetableRepository.getCurrentSemester()

                this.semesters = fetchedSemesters
                this.currentSemester = fetchedCurrent

                this.store = semesters.associate { semester ->
                    semester.id to listOf(makeMyTable(semester))
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun getMyTable(semesterId: String): Timetable {
        return store[semesterId]?.firstOrNull()
            ?: Timetable(lectures = emptyList())
    }

    private suspend fun makeMyTable(semester: Semester): Timetable {
        return Timetable(lectures = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType).lectures)
    }
}