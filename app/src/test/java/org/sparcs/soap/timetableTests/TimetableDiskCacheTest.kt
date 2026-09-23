package org.sparcs.soap.timetableTests

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.cache.AppDatabase
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.domain.enums.otl.SemesterType
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.helpers.TimetableSelectionStore
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableActivity
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.repositories.otl.OTLTimetableRepositoryProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCase
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.testSupport.MainDispatcherRule
import org.sparcs.soap.wearable.WearableDataManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class TimetableDiskCacheTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val context get() = RuntimeEnvironment.getApplication()
    private val semester = Semester.mockList().first()
    private val table = Timetable("44", listOf(Lecture.mock()), listOf(TimetableActivity(1, "Study", "", 0, 540, 600)))
    private val summaries get() = listOf(TimetableSummary(44, "Saved", semester.year, semester.semesterType))

    @Test fun offlineRestartRestoresSelectedTableAndDropdownFromDisk() = verifyRestart(44)
    @Test fun offlineRestartRestoresMyTableFromDisk() = verifyRestart(null)

    private fun verifyRestart(selectedID: Int?) = runBlocking {
        val name = "restart-${selectedID}.db"
        fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        fun useCase(database: AppDatabase, repository: Repository) = TimetableUseCase(
            repository, null, TimetableCache(database.timetableCacheDao()), WearableDataManager(context),
        )
        val online = Repository()
        val database = open()
        try {
            val useCase = useCase(database, online)
            useCase.refreshSemesters()
            useCase.refreshCurrentSemester()
            useCase.refreshTimetableList(semester)
            useCase.getTable(44, true)
            TimetableCache(database.timetableCacheDao()).store(table.copy(id = "myTable"), "${semester.id}-myTable")
            TimetableSelectionStore(context).save(semester, selectedID)
        } finally {
            database.close()
        }
        val reopened = open()
        try {
            val offline = Repository().apply { disconnected = true }
            val model = TimetableViewModel(useCase(reopened, offline), MockCrashlyticsService(), MockAnalyticsService(), context)
            withTimeout(10_000) { model.loadState.first { !it.isRefreshing } }
            assertEquals(semester, model.selectedSemester.value)
            assertEquals(selectedID, model.selectedTimetableID.value)
            assertEquals(if (selectedID == null) "myTable" else "44", model.selectedTimetable.value?.id)
            assertEquals(table.lectures, model.selectedTimetable.value?.lectures)
            assertEquals(table.activities, model.selectedTimetable.value?.activities)
            assertEquals(summaries, model.timetableList.value)
            assertTrue(model.loadState.value.isOffline)
            assertTrue(model.loadState.value.isReadOnly)
        } finally {
            reopened.close()
            context.deleteDatabase(name)
            TimetableSelectionStore(context).clear()
        }
    }

    private inner class Repository : OTLTimetableRepositoryProtocol {
        var disconnected = false
        private fun checkConnection() { if (disconnected) throw NetworkError.NoConnection() }
        override suspend fun getSemesters(): List<Semester> { checkConnection(); return listOf(semester) }
        override suspend fun getCurrentSemester(): Semester { checkConnection(); return semester }
        override suspend fun getTimetables(year: Int, semester: SemesterType): List<TimetableSummary> { checkConnection(); return summaries }
        override suspend fun getTimetable(timetableID: Int): Timetable { checkConnection(); return table }
        override suspend fun getMyTimetable(year: Int, semester: SemesterType): Timetable { checkConnection(); return table.copy(id = "myTable") }
        override suspend fun saveActivity(timetableID: Int, activityID: Int?, draft: ActivityDraft) = error("Unexpected write")
        override suspend fun deleteActivity(timetableID: Int, activityID: Int) = error("Unexpected write")
        override suspend fun createTable(year: Int, semester: SemesterType): TimetableCreation = error("Unexpected write")
        override suspend fun deleteTable(timetableID: Int) = error("Unexpected write")
        override suspend fun renameTable(timetableID: Int, title: String) = error("Unexpected write")
        override suspend fun addLecture(timetableID: Int, lectureID: Int) = error("Unexpected write")
        override suspend fun deleteLecture(timetableID: Int, lectureID: Int) = error("Unexpected write")
    }
}
