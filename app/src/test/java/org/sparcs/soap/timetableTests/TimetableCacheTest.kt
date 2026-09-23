package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sparcs.soap.app.cache.CachedTimetable
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.cache.TimetableCacheDAO
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.shared.mocks.otl.mockList
import java.util.Date

class TimetableCacheTest {
    private val dao = MemoryDAO()
    private val cache = TimetableCache(dao)
    private val semester = Semester.mockList().first()

    @Test fun newCacheInstanceRestoresNavigationAndTimestamp() = runTest {
        cache.storeSemesters(listOf(semester))
        cache.storeCurrentSemester(semester)
        cache.storeTimetableSummaries(emptyList(), semester)
        dao.saveTimetable(CachedTimetable("44", "{\"id\":\"44\",\"lectures\":[]}".toByteArray(), Date(1)))
        val restored = TimetableCache(dao).state(semester, 44)
        assertEquals(listOf(semester), restored.semesters)
        assertEquals(semester, restored.currentSemester)
        assertEquals(emptyList<TimetableSummary>(), restored.timetables)
        assertEquals("44", restored.timetable?.id)
        assertEquals(Date(1), restored.updatedAt)
    }

    @Test fun renameAndDeleteUpdateSavedNavigation() = runTest {
        cache.storeTimetableSummaries(listOf(TimetableSummary(44, "Old", semester.year, semester.semesterType)), semester)
        cache.store(Timetable("44", emptyList()), "44")
        cache.updateTimetableSummary(44, "Renamed")
        assertEquals("Renamed", cache.timetableSummaries(semester)?.single()?.title)
        assertEquals("44", cache.timetable("44")?.id)
        cache.updateTimetableSummary(44, null)
        assertTrue(cache.timetableSummaries(semester)!!.isEmpty())
    }

    @Test fun corruptDataIsACacheMiss() = runTest {
        dao.saveTimetable(CachedTimetable("semesters", "invalid".toByteArray()))
        dao.saveTimetable(CachedTimetable("44", "invalid".toByteArray()))
        assertNull(cache.semesters())
        assertNull(cache.state(semester, 44).updatedAt)
    }

    @Test fun semesterRefreshPreservesTableTimestampAndDoesNotReusePreviousSemester() = runTest {
        dao.saveTimetable(CachedTimetable("${semester.id}-myTable", "{\"id\":\"saved\",\"lectures\":[]}".toByteArray(), Date(1)))
        cache.storeCurrentSemester(semester)
        assertEquals(Date(1), dao.getTimetable("current-myTable")?.updatedAt)
        cache.storeCurrentSemester(semester.copy(year = semester.year + 1))
        assertNull(cache.currentMyTable())
    }

    @Test fun responseStartedBeforeSignOutCannotRepopulateCache() = runTest {
        val started = CompletableDeferred<Unit>()
        val response = CompletableDeferred<Unit>()
        val request = async {
            runCatching {
                cache.withSession {
                    started.complete(Unit)
                    response.await()
                    cache.store(Timetable("44", emptyList()), "44")
                }
            }
        }
        started.await()
        cache.clear()
        response.complete(Unit)
        assertTrue(request.await().isFailure)
        assertNull(cache.timetable("44"))
    }

    private class MemoryDAO : TimetableCacheDAO {
        private val entries = mutableMapOf<String, CachedTimetable>()
        override suspend fun getTimetable(key: String) = entries[key]
        override suspend fun saveTimetable(timetable: CachedTimetable) { entries[timetable.cacheKey] = timetable }
        override suspend fun invalidate(key: String) { entries.remove(key) }
        override suspend fun getSummaries() = entries.values.filter { it.cacheKey.endsWith("-summaries") }
        override suspend fun clear() { entries.clear() }
    }
}
