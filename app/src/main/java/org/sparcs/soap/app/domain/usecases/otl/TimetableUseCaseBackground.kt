package org.sparcs.soap.app.domain.usecases.otl

import kotlinx.coroutines.CancellationException
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
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
    private val timetableCache: TimetableCache,
) : TimetableUseCaseBackgroundProtocol {

    override suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable {
        val cacheKey = "$year-${semesterType.name}-myTable"
        return loadTimetable(cacheKey) {
            otlTimetableRepository.getMyTimetable(year, semesterType)
        }
    }

    override suspend fun getTable(id: Int): Timetable {
        return loadTimetable(id.toString()) { otlTimetableRepository.getTimetable(id) }
    }

    private suspend fun loadTimetable(cacheKey: String, fetch: suspend () -> Timetable): Timetable {
        return try {
            fetch().also { timetableCache.store(it, cacheKey) }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            val canUseCache = error is NetworkError.NoConnection || error is NetworkError.Timeout ||
                (error is NetworkError.ServerError && error.code >= 500)
            if (canUseCache) timetableCache.timetable(cacheKey)?.let { return it }
            // Leave the last rendered widget intact when no fresh or cached table is available.
            throw error
        }
    }

    override suspend fun getCurrentSemester(): Semester {
        return otlTimetableRepository.getCurrentSemester()
    }
}
