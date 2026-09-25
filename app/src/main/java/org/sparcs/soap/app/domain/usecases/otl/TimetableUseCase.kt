package org.sparcs.soap.app.domain.usecases.otl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.domain.error.CrashContext
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.otl.TimetableUseCaseError
import org.sparcs.soap.app.domain.models.otl.ActivityConflictException
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.ActivityRefreshRequiredException
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.TableDuplication
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCachedState
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.services.CrashlyticsServiceProtocol
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.wearable.WearableDataManager
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface TimetableUseCaseProtocol {
    suspend fun cachedState(semester: Semester? = null, timetableID: Int? = null): TimetableCachedState = TimetableCachedState()
    suspend fun refreshSemesters(): List<Semester> = getSemesters()
    suspend fun refreshCurrentSemester(): Semester = getCurrentSemester()
    suspend fun refreshTimetableList(semester: Semester): List<TimetableSummary> = getTimetableList(semester)

    suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft): Timetable
    suspend fun deleteActivity(timetableID: Int, activityID: Int): Timetable

    suspend fun getSemesters(): List<Semester>

    suspend fun getCurrentSemester(): Semester

    suspend fun getTimetableList(semester: Semester): List<TimetableSummary>

    suspend fun getTable(id: Int, forceRefresh: Boolean = false): Timetable

    suspend fun getMyTable(semester: Semester, forceRefresh: Boolean = false): Timetable

    suspend fun deleteTable(id: Int)

    suspend fun renameTable(id: Int, title: String)

    suspend fun createTable(semester: Semester): TimetableCreation

    /** Creates a new table for the semester holding a copy of the semester's "my table". */
    suspend fun duplicateMyTable(semester: Semester, title: String): TableDuplication

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
    private val externalScope = CoroutineScope(Dispatchers.IO)
    private val updateJobs = ConcurrentHashMap<String, Job>()

    override suspend fun cachedState(semester: Semester?, timetableID: Int?): TimetableCachedState =
        timetableCache.state(semester, timetableID)

    override suspend fun getSemesters(): List<Semester> {
        timetableCache.semesters()?.let {
            launchUpdate("semesters") { refreshSemesters() }
            return it
        }
        return refreshSemesters()
    }

    override suspend fun refreshSemesters(): List<Semester> = timetableCache.withSession {
        val result = otlTimetableRepository.getSemesters()
        currentCoroutineContext().ensureActive()
        timetableCache.storeSemesters(result)
        result
    }

    override suspend fun getCurrentSemester(): Semester {
        timetableCache.currentSemester()?.let {
            launchUpdate("current-semester") { refreshCurrentSemester() }
            return it
        }
        return refreshCurrentSemester()
    }

    override suspend fun refreshCurrentSemester(): Semester = timetableCache.withSession {
        val result = otlTimetableRepository.getCurrentSemester()
        currentCoroutineContext().ensureActive()
        timetableCache.storeCurrentSemester(result)
        result
    }

    override suspend fun getTimetableList(semester: Semester): List<TimetableSummary> {
        timetableCache.timetableSummaries(semester)?.let {
            launchUpdate("${semester.id}-summaries") { refreshTimetableList(semester) }
            return it
        }
        return refreshTimetableList(semester)
    }

    override suspend fun refreshTimetableList(semester: Semester): List<TimetableSummary> = timetableCache.withSession {
        val result = otlTimetableRepository.getTimetables(semester.year, semester.semesterType)
        currentCoroutineContext().ensureActive()
        timetableCache.storeTimetableSummaries(result, semester)
        result
    }

    override suspend fun getTable(id: Int, forceRefresh: Boolean): Timetable {

        val key = id.toString()
        val context = CrashContext(feature, metadata = mapOf("timetableID" to key))

        return execute(context) {
            try {
                val result = otlTimetableRepository.getTimetable(id)
                currentCoroutineContext().ensureActive()
                timetableCache.store(result, key)
                wearableDataManager.pushToWatchIfSelected(result, timetableID = id)
                result
            } catch (e: Exception) {
                if (e is CancellationException || forceRefresh || !(e is NetworkError.NoConnection || e is NetworkError.Timeout || (e is NetworkError.ServerError && e.code >= 500))) throw e
                // Offline launch can still show the last complete table, including activities.
                timetableCache.timetable(key) ?: throw e
            }
        }
    }

    override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable = timetableCache.withSession {
        val key = "${semester.id}-myTable"
        if (!forceRefresh) {
            timetableCache.timetable(key)?.let {
                launchUpdate(key) { getMyTable(semester, forceRefresh = true) }
                return@withSession it
            }
        }
        val result = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType)
        currentCoroutineContext().ensureActive()
        timetableCache.store(result, key)
        wearableDataManager.pushToWatchIfSelected(
            result, semester = semester, currentSemester = timetableCache.currentSemester()
        )
        result
    }

    override suspend fun deleteTable(id: Int) {
        val context = CrashContext(feature, metadata = mapOf("timetableID" to id.toString()))
        execute(context) {
            otlTimetableRepository.deleteTable(id)
            timetableCache.invalidate(id.toString())
            timetableCache.updateTimetableSummary(id, null)
        }
    }

    override suspend fun renameTable(id: Int, title: String) {
        val context = CrashContext(
            feature,
            metadata = mapOf("timetableID" to id.toString(), "title" to title)
        )
        execute(context) {
            otlTimetableRepository.renameTable(id, title)
            timetableCache.updateTimetableSummary(id, title)
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

    override suspend fun duplicateMyTable(semester: Semester, title: String): TableDuplication {
        val context = CrashContext(
            feature,
            metadata = mapOf(
                "year" to semester.year.toString(),
                "semester" to semester.semesterType.toString()
            )
        )

        return execute(context) {
            // Copy from the server, not the cache, so the duplicate matches what the user sees on
            // other devices too.
            val source = otlTimetableRepository.getMyTimetable(semester.year, semester.semesterType)
            val creation = otlTimetableRepository.createTable(semester.year, semester.semesterType)

            var skippedLectures = 0
            for (lecture in source.lectures) {
                currentCoroutineContext().ensureActive()
                try {
                    otlTimetableRepository.addLecture(creation.id, lecture.id)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    skippedLectures += 1
                    crashlyticsService?.record(e, context)
                }
            }

            var skippedActivities = 0
            for (activity in source.activities) {
                currentCoroutineContext().ensureActive()
                try {
                    otlTimetableRepository.saveActivity(creation.id, null, activity.draft())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    skippedActivities += 1
                    crashlyticsService?.record(e, context)
                }
            }

            if (title.isNotBlank()) {
                // A failed rename leaves a usable, correctly populated table.
                try {
                    otlTimetableRepository.renameTable(creation.id, title)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    crashlyticsService?.record(e, context)
                }
            }

            timetableCache.invalidate(creation.id.toString())

            TableDuplication(
                id = creation.id,
                skippedLectureCount = skippedLectures,
                skippedActivityCount = skippedActivities
            )
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
            wearableDataManager.pushToWatchIfSelected(freshTable, timetableID = timetableID)
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
            wearableDataManager.pushToWatchIfSelected(freshTable, timetableID = timetableID)
        }
    }

    override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft): Timetable = timetableCache.withSession {
        require(draft.isValid)
        updateJobs[timetableID.toString()]?.cancel()
        val fresh = getTable(timetableID, forceRefresh = true)
        if (draft.conflict(fresh, activityID)) throw ActivityConflictException()
        otlTimetableRepository.saveActivity(timetableID, activityID, draft.copy(title = draft.title.trim(), location = draft.location.trim()))
        refreshAfterActivityWrite(timetableID)
    }

    override suspend fun deleteActivity(timetableID: Int, activityID: Int): Timetable = timetableCache.withSession {
        updateJobs[timetableID.toString()]?.cancel()
        otlTimetableRepository.deleteActivity(timetableID, activityID)
        refreshAfterActivityWrite(timetableID)
    }

    private suspend fun refreshAfterActivityWrite(timetableID: Int): Timetable {
        timetableCache.invalidate(timetableID.toString())
        return try { getTable(timetableID, forceRefresh = true) }
        catch (e: Exception) {
            if (e is CancellationException) throw e
            throw ActivityRefreshRequiredException(e)
        }
    }

    private suspend fun launchUpdate(key: String, block: suspend CoroutineScope.() -> Unit) {
        if (updateJobs[key]?.isActive == true) return

        val session = timetableCache.sessionContext()
        updateJobs[key] = externalScope.launch(session) {
            try {
                timetableCache.withSession { block() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                try {
                    Timber.e(e, "Background update failed for key: $key")
                } catch (e: Exception) {
                    Timber.e(e, "Update failed")
                }
            }
        }
    }

    private suspend fun <T> execute(context: CrashContext, operation: suspend () -> T): T {
        return try {
            timetableCache.withSession { operation() }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val mappedError = e as? NetworkError ?: TimetableUseCaseError.Unknown(e)
            crashlyticsService?.record(mappedError as Throwable, context)
            throw mappedError
        }
    }
}

class MockTimetableUseCase(
    private val defaultTimetable: Timetable = Timetable.mock(),
    private val defaultSemesters: List<Semester> = Semester.mockList(),
    private val defaultSummaries: List<TimetableSummary> = TimetableSummary.mockList()
) : TimetableUseCaseProtocol {

    override suspend fun saveActivity(
        timetableID: Int,
        activityID: Int?,
        draft: ActivityDraft
    ): Timetable = defaultTimetable

    override suspend fun deleteActivity(
        timetableID: Int,
        activityID: Int
    ): Timetable = defaultTimetable

    override suspend fun getSemesters(): List<Semester> = defaultSemesters

    override suspend fun getCurrentSemester(): Semester =
        defaultSemesters.firstOrNull() ?: Semester.mockList().first()

    override suspend fun getTimetableList(semester: Semester): List<TimetableSummary> =
        defaultSummaries

    override suspend fun getTable(id: Int, forceRefresh: Boolean): Timetable =
        defaultTimetable

    override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable =
        defaultTimetable

    override suspend fun deleteTable(id: Int) {}

    override suspend fun renameTable(id: Int, title: String) {}

    override suspend fun createTable(semester: Semester): TimetableCreation =
        TimetableCreation(id = 1)

    override suspend fun duplicateMyTable(semester: Semester, title: String): TableDuplication =
        TableDuplication(id = 1, skippedLectureCount = 0, skippedActivityCount = 0)

    override suspend fun addLecture(timetableID: Int, lectureID: Int) {}

    override suspend fun deleteLecture(timetableID: Int, lectureID: Int) {}
}
