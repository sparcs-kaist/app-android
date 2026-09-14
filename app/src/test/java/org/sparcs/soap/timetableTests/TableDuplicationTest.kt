package org.sparcs.soap.timetableTests

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.cache.CachedTimetable
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.cache.TimetableCacheDAO
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.otl.*
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCase
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.wearable.WearableDataManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TableDuplicationTest {
    private val semester = Semester.mockList().first()
    private val repository = Repository()
    private val dao = CacheDAO()
    private val cache = TimetableCache(dao)
    private fun useCase() =
        TimetableUseCase(repository, null, cache, WearableDataManager(RuntimeEnvironment.getApplication()))

    @Test fun replaysEveryLectureAndActivityIntoTheNewTable() = runBlocking {
        val result = useCase().duplicateMyTable(semester, "My Table Copy")

        assertEquals(77, result.id)
        assertTrue(result.isComplete)
        assertEquals(repository.source.lectures.map { it.id }, repository.addedLectures.map { it.second })
        assertTrue(repository.addedLectures.all { it.first == 77 })
        assertEquals(listOf("Study" to 77), repository.addedActivities.map { it.second.title to it.first })
        assertEquals(77 to "My Table Copy", repository.renamed)
    }

    @Test fun copiesTheServerTableRatherThanTheCachedOne() = runBlocking {
        // The cache can hold an older copy from another device; the duplicate must not inherit it.
        cache.store(Timetable("myTable", emptyList()), "${semester.year}-${semester.semesterType.name}-myTable")

        useCase().duplicateMyTable(semester, "My Table Copy")

        assertEquals(repository.source.lectures.size, repository.addedLectures.size)
        assertTrue(repository.calls.first() == "getMyTimetable")
    }

    @Test fun rejectedItemsAreSkippedAndCountedInsteadOfFailingTheCopy() = runBlocking {
        repository.rejectLectureIDs = setOf(repository.source.lectures.first().id)
        repository.rejectActivities = true

        val result = useCase().duplicateMyTable(semester, "My Table Copy")

        assertEquals(77, result.id)
        assertEquals(1, result.skippedLectureCount)
        assertEquals(1, result.skippedActivityCount)
        assertFalse(result.isComplete)
        // Everything the server did accept is still there.
        assertEquals(repository.source.lectures.size - 1, repository.addedLectures.size)
    }

    @Test fun aFailedRenameStillLeavesAUsableTable() = runBlocking {
        repository.rejectRename = true

        val result = useCase().duplicateMyTable(semester, "My Table Copy")

        assertEquals(77, result.id)
        assertTrue(result.isComplete)
        assertNull(repository.renamed)
        assertEquals(repository.source.lectures.size, repository.addedLectures.size)
    }

    @Test fun anEmptyTitleLeavesTheServerNameUntouched() = runBlocking {
        useCase().duplicateMyTable(semester, "")

        assertNull(repository.renamed)
    }

    @Test fun aFailedCreateReportsTheErrorWithoutCopyingAnything() = runBlocking {
        repository.rejectCreate = true

        try {
            useCase().duplicateMyTable(semester, "My Table Copy")
            fail("Expected the create failure to surface")
        } catch (_: NetworkError.NoConnection) {
        }
        assertTrue(repository.addedLectures.isEmpty())
    }

    private class CacheDAO : TimetableCacheDAO {
        val records = mutableMapOf<String, CachedTimetable>()
        override suspend fun getTimetable(key: String) = records[key]
        override suspend fun saveTimetable(timetable: CachedTimetable) { records[timetable.cacheKey] = timetable }
        override suspend fun invalidate(key: String) { records.remove(key) }
    }

    private class Repository : OTLTimetableRepositoryProtocol {
        val source = Timetable(
            id = "myTable",
            lectures = Lecture.mockList().take(3),
            customBlocks = listOf(TimetableActivity(17, "Study", "Library", 0, 600, 660))
        )
        val calls = mutableListOf<String>()
        val addedLectures = mutableListOf<Pair<Int, Int>>()
        val addedActivities = mutableListOf<Pair<Int, ActivityDraft>>()
        var renamed: Pair<Int, String>? = null
        var rejectLectureIDs: Set<Int> = emptySet()
        var rejectActivities = false
        var rejectRename = false
        var rejectCreate = false

        override suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable {
            calls += "getMyTimetable"
            return source
        }

        override suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation {
            calls += "createTable"
            if (rejectCreate) throw NetworkError.NoConnection()
            return TimetableCreation(77)
        }

        override suspend fun addLecture(timetableID: Int, lectureID: Int) {
            if (lectureID in rejectLectureIDs) throw NetworkError.NoConnection()
            addedLectures += timetableID to lectureID
        }

        override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft) {
            if (rejectActivities) throw NetworkError.NoConnection()
            addedActivities += timetableID to draft
        }

        override suspend fun renameTable(timetableID: Int, title: String) {
            if (rejectRename) throw NetworkError.NoConnection()
            renamed = timetableID to title
        }

        override suspend fun getTimetable(timetableID: Int): Timetable = error("unused")
        override suspend fun deleteActivity(timetableID: Int, activityID: Int) = Unit
        override suspend fun getTimetables(year: Int, semester: SemesterType) = emptyList<TimetableSummary>()
        override suspend fun deleteTable(timetableID: Int) = Unit
        override suspend fun deleteLecture(timetableID: Int, lectureID: Int) = Unit
        override suspend fun getSemesters() = emptyList<Semester>()
        override suspend fun getCurrentSemester(): Semester = error("unused")
    }
}
