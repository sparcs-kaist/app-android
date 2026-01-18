package org.sparcs.App.Domain.Usecases

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sparcs.App.Domain.Models.OTL.OTLUser
import org.sparcs.App.Domain.Models.OTL.Semester
import org.sparcs.App.Domain.Models.OTL.Timetable
import org.sparcs.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
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
    private val userUseCase: UserUseCaseProtocol,
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

                val user = userUseCase.otlUser ?: run {
                    userUseCase.fetchOTLUser()
                    userUseCase.otlUser
                }

                this.store = semesters.associate { semester ->
                    semester.id to listOf(makeMyTable(semester, user))
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun getMyTable(semesterId: String): Timetable {
        return store[semesterId]?.firstOrNull()
            ?: Timetable(id = "$semesterId-myTable", lectures = emptyList())
    }

    private fun makeMyTable(semester: Semester, user: OTLUser?): Timetable {
        val lectures = user?.myTimetableLectures?.filter {
            it.year == semester.year && it.semester == semester.semesterType
        } ?: emptyList()

        return Timetable(id = "${semester.id}-myTable", lectures = lectures)
    }
}