package org.sparcs.soap.timetableTests

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.cache.*
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCase
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.wearable.WearableDataManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ActivityUseCaseTest {
    private val repository = Repository()
    private val dao = CacheDAO()
    private val cache = TimetableCache(dao)
    private fun useCase() = TimetableUseCase(repository, null, cache, WearableDataManager(RuntimeEnvironment.getApplication()))

    @Test fun createRefetchesAndCachesServerResult() = runBlocking {
        val result = useCase().saveActivity(12, null, ActivityDraft("Study"))
        assertEquals(listOf("get", "create", "get"), repository.calls)
        assertEquals(41, result.activities.single().id)
        assertEquals(result.activities, cache.timetable("12")!!.activities)
    }
    @Test fun editAndDeleteRefetch() = runBlocking {
        val useCase = useCase()
        useCase.saveActivity(12, null, ActivityDraft("Study"))
        repository.calls.clear()
        val edited = useCase.saveActivity(12, 41, ActivityDraft("Edited"))
        assertEquals("Edited", edited.activities.single().title)
        assertEquals(listOf("get", "edit", "get"), repository.calls)
        repository.calls.clear()
        assertTrue(useCase.deleteActivity(12, 41).activities.isEmpty())
        assertEquals(listOf("delete", "get"), repository.calls)
    }
    @Test fun conflictFromFreshServerDataPreventsWrite() = runBlocking {
        repository.table = repository.table.copy(customBlocks = listOf(TimetableActivity(9, "Busy", "", 0, 540, 600)))
        try { useCase().saveActivity(12, null, ActivityDraft("Study")); fail("Expected conflict") }
        catch (_: ActivityConflictException) { }
        assertEquals(listOf("get"), repository.calls)
    }
    @Test fun successfulWriteWithFailedRefreshNeverRepeatsPost() = runBlocking {
        val useCase = useCase()
        repository.failRefresh = true
        try { useCase.saveActivity(12, null, ActivityDraft("Study")); fail("Expected refresh error") }
        catch (_: ActivityRefreshRequiredException) { }
        assertNull(cache.timetable("12"))
        repository.failRefresh = false
        assertEquals(1, useCase.getTable(12, true).activities.size)
        assertEquals(1, repository.calls.count { it == "create" })
    }
    @Test fun offlineUsesCachedActivitiesButAuthorizationErrorsDoNot() = runBlocking {
        val useCase = useCase()
        useCase.saveActivity(12, null, ActivityDraft("Study"))
        repository.failure = NetworkError.NoConnection()
        assertEquals(1, useCase.getTable(12).activities.size)
        repository.failure = NetworkError.Unauthorized()
        try { useCase.getTable(12); fail("Expected unauthorized") } catch (_: NetworkError.Unauthorized) { }
    }

    private class CacheDAO : TimetableCacheDAO {
        val records = mutableMapOf<String, CachedTimetable>()
        override suspend fun getTimetable(key: String) = records[key]
        override suspend fun saveTimetable(timetable: CachedTimetable) { records[timetable.cacheKey] = timetable }
        override suspend fun invalidate(key: String) { records.remove(key) }
    }
    private class Repository : OTLTimetableRepositoryProtocol {
        var table = Timetable("12", emptyList())
        val calls = mutableListOf<String>()
        var failRefresh = false
        var failure: Exception? = null
        override suspend fun getTimetable(timetableID: Int): Timetable {
            calls += "get"
            failure?.let { throw it }
            if (failRefresh && table.activities.isNotEmpty()) throw NetworkError.NoConnection()
            return table
        }
        override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft) {
            calls += if (activityID == null) "create" else "edit"
            table = table.copy(customBlocks = listOf(TimetableActivity(activityID ?: 41, draft.title, draft.location, draft.day, draft.begin, draft.end)))
        }
        override suspend fun deleteActivity(timetableID: Int, activityID: Int) { calls += "delete"; table = table.copy(customBlocks = emptyList()) }
        override suspend fun getTimetables(year: Int, semester: SemesterType) = emptyList<TimetableSummary>()
        override suspend fun getMyTimetable(year: Int, semester: SemesterType) = Timetable("myTable", emptyList())
        override suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation = error("unused")
        override suspend fun deleteTable(timetableID: Int) = Unit
        override suspend fun renameTable(timetableID: Int, title: String) = Unit
        override suspend fun addLecture(timetableID: Int, lectureID: Int) = Unit
        override suspend fun deleteLecture(timetableID: Int, lectureID: Int) = Unit
        override suspend fun getSemesters() = emptyList<Semester>()
        override suspend fun getCurrentSemester(): Semester = error("unused")
    }
}
