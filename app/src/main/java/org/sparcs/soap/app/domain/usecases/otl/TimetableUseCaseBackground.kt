package org.sparcs.soap.app.domain.usecases.otl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.canKeepSavedTimetable
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseBackgroundProtocol {
    suspend fun getMyTable(year: Int, semesterType: SemesterType): Timetable
    suspend fun getTable(id: Int): Timetable
    suspend fun getCurrentMyTable(): Timetable {
        val semester = getCurrentSemester() ?: throw NetworkError.NoConnection()
        return getMyTable(semester.year, semester.semesterType)
    }
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
        return timetableCache.withSession { try {
            fetch().also {
                currentCoroutineContext().ensureActive()
                timetableCache.store(it, cacheKey)
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            val canUseCache = error.canKeepSavedTimetable()
            if (canUseCache) timetableCache.timetable(cacheKey)?.let { return@withSession it }
            // Leave the last rendered widget intact when no fresh or cached table is available.
            throw error
        } }
    }

    override suspend fun getCurrentMyTable(): Timetable = timetableCache.withSession {
        val semester = try {
            otlTimetableRepository.getCurrentSemester().also {
                currentCoroutineContext().ensureActive()
                timetableCache.storeCurrentSemester(it)
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (e.canKeepSavedTimetable()) timetableCache.currentMyTable()?.let { return@withSession it }
            throw e
        }
        getMyTable(semester.year, semester.semesterType).also {
            timetableCache.storeCurrentSemester(semester)
        }
    }

    override suspend fun getCurrentSemester(): Semester = timetableCache.withSession {
        try {
            otlTimetableRepository.getCurrentSemester().also {
                currentCoroutineContext().ensureActive()
                timetableCache.storeCurrentSemester(it)
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) {
            if (e.canKeepSavedTimetable()) timetableCache.currentSemester()?.let { return@withSession it }
            throw e
        }
    }
}
