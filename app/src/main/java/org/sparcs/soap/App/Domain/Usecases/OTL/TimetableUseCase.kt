package org.sparcs.soap.App.Domain.Usecases.OTL

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Cache.TimetableCache
import org.sparcs.soap.App.Domain.Error.CrashContext
import org.sparcs.soap.App.Domain.Error.NetworkError
import org.sparcs.soap.App.Domain.Error.OTL.TimetableUseCaseError
import org.sparcs.soap.App.Domain.Models.OTL.Semester
import org.sparcs.soap.App.Domain.Models.OTL.Timetable
import org.sparcs.soap.App.Domain.Models.OTL.TimetableCreation
import org.sparcs.soap.App.Domain.Models.OTL.TimetableSummary
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLTimetableRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.CrashlyticsServiceProtocol
import org.sparcs.soap.Wearable.WearableDataManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseProtocol {
    suspend fun getSemesters(): List<Semester>

    suspend fun getCurrentSemester(): Semester

    suspend fun getTimetableList(semester: Semester): List<TimetableSummary>

    suspend fun getTable(id: Int, forceRefresh: Boolean = false): Timetable

    suspend fun getMyTable(semester: Semester, forceRefresh: Boolean = false): Timetable

    suspend fun deleteTable(id: Int)

    suspend fun renameTable(id: Int, title: String)

    suspend fun createTable(semester: Semester): TimetableCreation

    suspend fun addLecture(timetableID: Int, lectureID: Int)

    suspend fun deleteLecture(timetableID: Int, lectureID: Int)
}

@Singleton
class TimetableUseCase @Inject constructor(
    private val otlTimetableRepository: OTLTimetableRepositoryProtocol,
    private val crashlyticsService: CrashlyticsServiceProtocol? = null,
    private val timetableCache: TimetableCache,
    private val wearableDataManager: WearableDataManager,
) : TimetableUseCaseProtocol {
    // MARK: - Properties
    private val feature: String = "Timetable"

    // MARK: - Cached State
    private val semesterCache = SemesterCache()
    private val externalScope = CoroutineScope(Dispatchers.IO)
    private val updateJobs = mutableMapOf<String, Job>()

    override suspend fun getSemesters(): List<Semester> {
        val context = CrashContext(feature)
        return execute(context) {
            semesterCache.getSemesters() ?: run {
                val result = otlTimetableRepository.getSemesters()
                semesterCache.setSemesters(result)
                result
            }
        }
    }

    // MARK: - Functions
    override suspend fun getCurrentSemester(): Semester {
        val context = CrashContext(feature)
        return execute(context) {
            semesterCache.getCurrentSemester() ?: run {
                val result = otlTimetableRepository.getCurrentSemester()
                semesterCache.setCurrentSemester(result)
                result
            }
        }
    }

    override suspend fun getTimetableList(semester: Semester): List<TimetableSummary> {
        val context = CrashContext(
            feature,
            metadata = mapOf(
                "year" to semester.year.toString(),
                "semester" to semester.semesterType.toString()
            )
        )

        return execute(context) {
            otlTimetableRepository.getTimetables(semester.year, semester.semesterType)
        }
    }

    override suspend fun getTable(id: Int, forceRefresh: Boolean): Timetable {

        val key = id.toString()
        val context = CrashContext(feature, metadata = mapOf("timetableID" to key))

        return execute(context) {
            if (!forceRefresh) {
                timetableCache.timetable(key)?.let { cached ->
                    launchUpdate(key) {
                        val fresh = otlTimetableRepository.getTimetable(id)
                        timetableCache.store(fresh, key)
                    }
                    return@execute cached
                }
            }

            val result = otlTimetableRepository.getTimetable(id)
            timetableCache.store(result, key)
            result
        }
    }

    override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable {
        val context = CrashContext(
            feature, metadata = mapOf(
                "year" to semester.year.toString(),
                "semester" to semester.semesterType.toString()
            )
        )
        val key = "${semester.year}-${semester.semesterType.name}-myTable"

        return execute(context) {
            if (!forceRefresh) {
                timetableCache.timetable(key)?.let { cached ->
                    launchUpdate(key) {
                        val freshDef = async {
                            runCatching { otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType) }.getOrNull()
                        }
                        val currentDef = async { runCatching { otlTimetableRepository.getCurrentSemester() }.getOrNull() }

                        val fresh = freshDef.await()
                        val current = currentDef.await()

                        fresh?.let {
                            timetableCache.store(it, key)
                        }
                        if (current?.let { it.year == semester.year && it.semesterType == semester.semesterType} == true) {
                            fresh?.let { wearableDataManager.sendTimetableToWatch(it) }
                        }
                    }
                    return@execute cached
                }
            }

            val result = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType)
            timetableCache.store(result, key)

            launchUpdate(key) {
                val current = runCatching { otlTimetableRepository.getCurrentSemester() }.getOrNull()
                if (current?.year == semester.year && current.semesterType == semester.semesterType) {
                    wearableDataManager.sendTimetableToWatch(result)
                }
            }
            result
        }
    }

    override suspend fun deleteTable(id: Int) {
        val context = CrashContext(feature, metadata = mapOf("timetableID" to id.toString()))
        execute(context) {
            otlTimetableRepository.deleteTable(id)
            timetableCache.invalidate(id.toString())
        }
    }

    override suspend fun renameTable(id: Int, title: String) {
        val context = CrashContext(
            feature,
            metadata = mapOf("timetableID" to id.toString(), "title" to title)
        )
        execute(context) {
            otlTimetableRepository.renameTable(id, title)
            timetableCache.invalidate(id.toString())
        }
    }

    override suspend fun createTable(semester: Semester): TimetableCreation {
        val context = CrashContext(
            feature,
            metadata = mapOf(
                "year" to semester.year.toString(),
                "semester" to semester.semesterType.toString()
            )
        )
        return execute(context) {
            otlTimetableRepository.createTable(semester.year, semester.semesterType)
        }
    }

    override suspend fun addLecture(timetableID: Int, lectureID: Int) {
        val context = CrashContext(
            feature,
            metadata = mapOf(
                "timetableID" to timetableID.toString(),
                "lectureID" to lectureID.toString()
            )
        )
        execute(context) {
            otlTimetableRepository.addLecture(timetableID, lectureID)
            val freshTable = otlTimetableRepository.getTimetable(timetableID)
            timetableCache.store(freshTable, timetableID.toString())
        }
    }

    override suspend fun deleteLecture(timetableID: Int, lectureID: Int) {
        val context = CrashContext(
            feature,
            metadata = mapOf(
                "timetableID" to timetableID.toString(),
                "lectureID" to lectureID.toString()
            )
        )
        execute(context) {
            otlTimetableRepository.deleteLecture(timetableID, lectureID)
            val freshTable = otlTimetableRepository.getTimetable(timetableID)
            timetableCache.store(freshTable, timetableID.toString())
        }
    }

    private fun launchUpdate(key: String, block: suspend CoroutineScope.() -> Unit) {
        if (updateJobs[key]?.isActive == true) return

        updateJobs[key] = externalScope.launch {
            try {
                block()
            } catch (e: Exception) {
                try {
                    Timber.e(e, "Background update failed for key: $key")
                } catch (t: Exception) {
                    Timber.e("TimetableUseCase", "Update failed", e)
                }
            }
        }
    }

    private suspend fun <T> execute(context: CrashContext, operation: suspend () -> T): T {
        return try {
            operation()
        } catch (e: Exception) {
            val mappedError = if (e is NetworkError) e else TimetableUseCaseError.Unknown(e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}

// MARK: - SemesterCache Actor
private class SemesterCache {
    @Volatile
    private var semesters: List<Semester>? = null

    @Volatile
    private var currentSemester: Semester? = null

    fun getSemesters() = semesters
    fun setSemesters(value: List<Semester>) {
        semesters = value
    }

    fun getCurrentSemester() = currentSemester
    fun setCurrentSemester(value: Semester) {
        currentSemester = value
    }
}