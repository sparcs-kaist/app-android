package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.helpers.TimetableSelectionStore
import org.sparcs.soap.app.domain.models.otl.ActivityDraft
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCachedState
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockTimetableUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class TimetableOfflineTest {
    @get:Rule val dispatcher = MainDispatcherRule()
    private val context get() = RuntimeEnvironment.getApplication()
    private val semester = Semester.mockList().first()
    private val saved = Timetable("44", emptyList())
    private val api = OfflineUseCase()

    @Before fun prepare() {
        TimetableSelectionStore(context).clear()
        TimetableSelectionStore(context).save(semester, 44)
    }

    private fun model() = TimetableViewModel(api, MockCrashlyticsService(), MockAnalyticsService(), context)

    @Test fun coldOfflineLaunchRestoresContentsAndBlocksWrites() = runTest {
        val model = model()
        assertEquals(semester, model.selectedSemester.value)
        assertEquals(44, model.selectedTimetableID.value)
        assertEquals(saved, model.selectedTimetable.value)
        assertEquals("Saved", model.timetableList.value.first().title)
        assertEquals(Date(1), model.loadState.value.lastUpdated)
        assertTrue(model.loadState.value.isReadOnly)
        assertTrue(model.loadState.value.isOffline)
        assertFalse(model.showAlert)
        model.renameTable("Changed")
        model.deleteTable()
        model.createTable()
        model.duplicateMyTable()
        assertTrue(runCatching { model.saveActivity(44, null, ActivityDraft("Study")) }.exceptionOrNull() is NetworkError.NoConnection)
        assertTrue(runCatching { model.deleteActivity(44, 1) }.exceptionOrNull() is NetworkError.NoConnection)
        assertEquals(0, api.writes)
    }

    @Test fun reconnectReplacesSavedDataAndEnablesEditing() = runTest {
        val model = model()
        model.connectivityChanged(false)
        api.failure = null
        model.connectivityChanged(true)
        assertEquals("44", model.selectedTimetable.value?.id)
        assertFalse(model.loadState.value.isShowingSavedData)
        assertFalse(model.loadState.value.isReadOnly)
        assertTrue(model.isEditable.value)
        assertNull(model.loadState.value.loadError)
        assertEquals("Fresh", model.timetableList.value.single().title)
    }

    @Test fun reconnectDuringAnInFlightFailureRunsAnotherRefresh() = runTest {
        val model = model()
        val delayed = CompletableDeferred<Unit>()
        api.delayedSemesters = delayed
        model.fetchData()
        model.connectivityChanged(false)
        api.failure = null
        model.connectivityChanged(true)
        assertFalse(model.loadState.value.isOffline)
        api.delayedSemesters = null
        delayed.complete(Unit)
        assertEquals(3, api.semesterRequests)
        assertFalse(model.loadState.value.isReadOnly)
        assertFalse(model.loadState.value.isRefreshing)
    }

    @Test fun cacheMissNeverShowsPreviousSelection() = runTest {
        val model = model()
        model.selectTimetable(45)
        assertNull(model.selectedTimetable.value)
        assertNull(model.loadState.value.lastUpdated)
        assertNotNull(model.loadState.value.loadError)
        assertEquals(45, TimetableSelectionStore(context).selection?.timetableID)
    }

    @Test fun staleCachedListCannotEraseSelection() = runTest {
        api.cached = api.cached.copy(timetables = emptyList())
        val model = model()
        assertEquals(44, model.selectedTimetableID.value)
        assertEquals(saved, model.selectedTimetable.value)
        assertEquals(44, TimetableSelectionStore(context).selection?.timetableID)
    }

    @Test fun wrappedNetworkFailureKeepsCacheButUnauthorizedHidesIt() = runTest {
        api.failure = AuthUseCaseError.RefreshFailed(NetworkError.NoConnection())
        val model = model()
        assertEquals(saved, model.selectedTimetable.value)
        assertTrue(model.loadState.value.isOffline)
        api.failure = NetworkError.Unauthorized()
        model.fetchData()
        assertNull(model.selectedTimetable.value)
        assertNull(model.loadState.value.lastUpdated)
        assertTrue(model.loadState.value.isReadOnly)
    }

    @Test fun incompleteSemesterCachePreservesPreferenceUntilRecovery() = runTest {
        api.cached = api.cached.copy(semesters = emptyList())
        val model = model()
        assertNull(model.selectedSemester.value)
        assertEquals(44, TimetableSelectionStore(context).selection?.timetableID)
        api.failure = null
        model.fetchData()
        assertEquals(semester, model.selectedSemester.value)
        assertEquals(44, model.selectedTimetableID.value)
        assertFalse(model.loadState.value.isReadOnly)
    }

    @Test fun launchWithoutCacheCanRecover() = runTest {
        TimetableSelectionStore(context).clear()
        api.cached = TimetableCachedState()
        val model = model()
        assertNull(model.selectedTimetable.value)
        assertNotNull(model.loadState.value.loadError)
        api.failure = null
        model.fetchData()
        assertEquals("${semester.id}-myTable", model.selectedTimetable.value?.id)
        assertFalse(model.loadState.value.isReadOnly)
    }

    @Test fun lateResponseCannotOverwriteSelectionThatChangedAwayAndBack() = runTest {
        api.failure = null
        val model = model()
        val delayed = CompletableDeferred<Timetable>()
        api.delayedTable = delayed
        model.selectTimetable(45)
        api.delayedTable = null
        model.selectTimetable(44)
        delayed.complete(Timetable("45", emptyList()))
        assertEquals("44", model.selectedTimetable.value?.id)
        assertEquals(44, model.selectedTimetableID.value)
    }

    private inner class OfflineUseCase : TimetableUseCaseProtocol by MockTimetableUseCase() {
        var failure: Exception? = NetworkError.NoConnection()
        var writes = 0
        var delayedTable: CompletableDeferred<Timetable>? = null
        var delayedSemesters: CompletableDeferred<Unit>? = null
        var semesterRequests = 0
        var cached = TimetableCachedState(
            listOf(semester), semester,
            listOf(TimetableSummary(44, "Saved", semester.year, semester.semesterType)), saved, Date(1),
        )
        override suspend fun cachedState(semester: Semester?, timetableID: Int?) = cached.copy(
            timetable = cached.timetable?.takeIf { it.id == timetableID?.toString() },
        )
        override suspend fun refreshSemesters(): List<Semester> {
            semesterRequests++
            val failureAtStart = failure
            delayedSemesters?.await()
            failureAtStart?.let { throw it }
            return listOf(semester)
        }
        override suspend fun refreshCurrentSemester(): Semester {
            failure?.let { throw it }
            return semester
        }
        override suspend fun refreshTimetableList(semester: Semester): List<TimetableSummary> {
            failure?.let { throw it }
            return listOf(TimetableSummary(44, "Fresh", semester.year, semester.semesterType))
        }
        override suspend fun getTable(id: Int, forceRefresh: Boolean): Timetable {
            delayedTable?.let { return it.await() }
            failure?.let { throw it }
            return Timetable(id.toString(), emptyList())
        }
        override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable {
            failure?.let { throw it }
            return Timetable("${semester.id}-myTable", emptyList())
        }
        override suspend fun renameTable(id: Int, title: String) { writes++ }
        override suspend fun deleteTable(id: Int) { writes++ }
    }
}
