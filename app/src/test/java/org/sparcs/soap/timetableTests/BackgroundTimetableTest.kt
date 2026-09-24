package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Test
import org.sparcs.soap.app.cache.CachedTimetable
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.cache.TimetableCacheDAO
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseBackground
import org.sparcs.soap.app.shared.mocks.otl.mockList

class BackgroundTimetableTest {
    private val repository = TestRepository()
    private val cache = TimetableCache(MemoryTimetableCacheDAO())
    private val useCase = TimetableUseCaseBackground(repository, cache)
    private val timetable = Timetable(
        "2026-AUTUMN-myTable",
        emptyList(),
        listOf(TimetableActivity(1, "Study", "Library", 0, 600, 660))
    )

    @Test
    fun mainTimetableUsesCachedContentsWhenOffline() = runTest {
        cache.store(timetable, timetable.id)
        repository.failure = NetworkError.NoConnection()

        val result = useCase.getMyTable(2026, SemesterType.AUTUMN)

        assertEquals(timetable, result)
    }

    @Test
    fun failedFetchWithoutCacheDoesNotProduceAnEmptyTimetable() = runTest {
        val failure = NetworkError.Timeout()
        repository.failure = failure

        val result = runCatching { useCase.getMyTable(2026, SemesterType.AUTUMN) }

        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun currentWidgetWorksWhenSemesterLookupIsOffline() = runTest {
        cache.storeCurrentMyTable(timetable, Semester.mockList().first())
        assertEquals(timetable, useCase.getCurrentMyTable())
    }

    @Test
    fun authorizationFailureDoesNotReuseCurrentWidgetCache() = runTest {
        cache.storeCurrentMyTable(timetable, Semester.mockList().first())
        repository.failure = NetworkError.Unauthorized()
        assertTrue(runCatching { useCase.getCurrentMyTable() }.exceptionOrNull() is NetworkError.Unauthorized)
    }

    @Test
    fun cancellationPropagatesEvenWhenCacheExists() = runTest {
        cache.store(timetable, timetable.id)
        val cancellation = CancellationException("Widget update cancelled")
        repository.failure = cancellation

        val result = runCatching { useCase.getMyTable(2026, SemesterType.AUTUMN) }

        assertTrue(result.exceptionOrNull() is CancellationException)
        assertEquals(cancellation.message, result.exceptionOrNull()?.message)
    }

    private class MemoryTimetableCacheDAO : TimetableCacheDAO {
        override suspend fun getSummaries(): List<CachedTimetable> = emptyList()
        override suspend fun clear() {}

        private val entries = mutableMapOf<String, CachedTimetable>()

        override suspend fun getTimetable(key: String): CachedTimetable? = entries[key]
        override suspend fun saveTimetable(timetable: CachedTimetable) {
            entries[timetable.cacheKey] = timetable
        }

        override suspend fun invalidate(key: String) {
            entries.remove(key)
        }
    }

    private class TestRepository : OTLTimetableRepositoryProtocol {
        var failure: Exception = NetworkError.NoConnection()

        override suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable = throw failure
        override suspend fun getTimetable(timetableID: Int): Timetable = throw failure
        override suspend fun getCurrentSemester(): Nothing = throw failure
        override suspend fun getSemesters(): Nothing = error("Unexpected call")
        override suspend fun getTimetables(year: Int, semester: SemesterType): Nothing = error("Unexpected call")
        override suspend fun createTable(year: Int, semester: SemesterType): Nothing = error("Unexpected call")
        override suspend fun deleteTable(timetableID: Int): Unit = error("Unexpected call")
        override suspend fun renameTable(timetableID: Int, title: String): Unit = error("Unexpected call")
        override suspend fun addLecture(timetableID: Int, lectureID: Int): Unit = error("Unexpected call")
        override suspend fun deleteLecture(timetableID: Int, lectureID: Int): Unit = error("Unexpected call")
        override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft): Unit = error("Unexpected call")
        override suspend fun deleteActivity(timetableID: Int, activityID: Int): Unit = error("Unexpected call")
    }
}
