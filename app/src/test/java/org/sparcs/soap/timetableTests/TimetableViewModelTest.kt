package org.sparcs.soap.timetableTests

import android.content.Context
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
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TimetableSelectionStore
import org.sparcs.soap.app.domain.models.otl.TableDuplication
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCreation
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.domain.models.otl.Lecture
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.features.timetable.TimetableViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.MockAnalyticsService
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockTimetableUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class TimetableViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTimetableUseCase: MockTimetableUseCase
    private lateinit var viewModel: TimetableViewModel
    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Before
    fun setup() {
        TimetableSelectionStore(context).clear()
        mockTimetableUseCase = MockTimetableUseCase()
    }

    private fun createViewModel(useCase: TimetableUseCaseProtocol = mockTimetableUseCase) {
        viewModel = TimetableViewModel(
            timetableUseCase = useCase,
            crashlyticsService = MockCrashlyticsService(),
            analyticsService = MockAnalyticsService(),
            context = context,
        )
    }

    @Test
    fun `fetchData populates semesters and loads my table`() = runTest {
        val semesters = Semester.mockList()
        mockTimetableUseCase.getSemestersResult = Result.success(semesters)
        mockTimetableUseCase.getCurrentSemesterResult = Result.success(semesters.first())

        createViewModel()

        assertEquals(semesters, viewModel.semesters.value)
        assertEquals(semesters.first(), viewModel.selectedSemester.value)
        assertNotNull(viewModel.selectedTimetable.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchData failure shows alert`() = runTest {
        mockTimetableUseCase.getSemestersResult = Result.failure(Exception("Test failure"))

        createViewModel()

        assertTrue(viewModel.showAlert)
        assertNotNull(viewModel.alertMessageRes)
        assertNull(viewModel.selectedTimetable.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `setCandidateLecture updates candidate`() = runTest {
        createViewModel()
        val lecture = Lecture.mock()

        viewModel.setCandidateLecture(lecture)

        assertEquals(lecture, viewModel.candidateLecture.value)
    }

    @Test
    fun `selectTimetable loads the chosen table and deleteLecture delegates`() = runTest {
        createViewModel()

        viewModel.selectTimetable(5)
        assertEquals(5, viewModel.selectedTimetableID.value)
        assertNotNull(viewModel.selectedTimetable.value)

        viewModel.deleteLecture(Lecture.mock())
        assertEquals(1, mockTimetableUseCase.deleteLectureCallCount)
    }

    private fun configureDuplication(existingTitles: List<String> = emptyList()): List<Semester> {
        val semesters = Semester.mockList().take(3)
        mockTimetableUseCase.getSemestersResult = Result.success(semesters)
        mockTimetableUseCase.getCurrentSemesterResult = Result.success(semesters.last())
        val semester = semesters.last()
        mockTimetableUseCase.getTimetableListResult = Result.success(
            existingTitles.mapIndexed { index, title ->
                TimetableSummary(99 - index, title, semester.year, semester.semesterType)
            }
        )
        return semesters
    }

    @Test
    fun `duplicateMyTable selects the copy and names it without clashing`() = runTest {
        val copy = context.getString(R.string.timetable_duplicate_title)
        configureDuplication(listOf("$copy 2", copy))

        createViewModel()
        viewModel.duplicateMyTable()

        assertEquals(listOf("$copy 3"), mockTimetableUseCase.duplicatedTitles)
        assertEquals(99, viewModel.selectedTimetableID.value)
        assertFalse(viewModel.isDuplicatingTable.value)
        assertFalse(viewModel.showAlert)
    }

    @Test
    fun `an incomplete copy is reported but still selected`() = runTest {
        configureDuplication()
        mockTimetableUseCase.duplicateMyTableResult =
            Result.success(TableDuplication(id = 99, skippedLectureCount = 2))

        createViewModel()
        viewModel.duplicateMyTable()

        assertTrue(viewModel.showAlert)
        assertEquals(R.string.timetable_duplicate_partial_title, viewModel.alertTitleRes)
        assertEquals(R.string.timetable_duplicate_partial_message, viewModel.alertMessageRes)
    }

    @Test
    fun `a failed duplication shows an error and frees the menu entry`() = runTest {
        configureDuplication()
        mockTimetableUseCase.duplicateMyTableResult = Result.failure(Exception("Test failure"))

        createViewModel()
        viewModel.duplicateMyTable()

        assertTrue(viewModel.showAlert)
        assertNull(viewModel.alertTitleRes)
        assertEquals(R.string.error_duplicate_table, viewModel.alertMessageRes)
        assertFalse(viewModel.isDuplicatingTable.value)
    }

    private fun configureSelection(): List<Semester> {
        val semesters = Semester.mockList().take(3)
        mockTimetableUseCase.getSemestersResult = Result.success(semesters)
        mockTimetableUseCase.getCurrentSemesterResult = Result.success(semesters.last())
        mockTimetableUseCase.getTimetableListResult = Result.success(listOf(
            TimetableSummary(5, "Plan", semesters[1].year, semesters[1].semesterType)
        ))
        mockTimetableUseCase.getTableResult = Result.success(Timetable("5", emptyList()))
        mockTimetableUseCase.getMyTableResult = Result.success(Timetable("my", emptyList()))
        return semesters
    }

    @Test
    fun `new view model restores the chosen semester and timetable`() = runTest {
        val semesters = configureSelection()
        createViewModel()
        viewModel.selectPreviousSemester()
        viewModel.selectTimetable(5)

        createViewModel()

        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertEquals(5, viewModel.selectedTimetableID.value)
        assertEquals("5", viewModel.selectedTimetable.value?.id)
    }

    @Test
    fun `choosing My Table replaces the saved table choice`() = runTest {
        val semesters = configureSelection()
        createViewModel()
        viewModel.selectPreviousSemester()
        viewModel.selectTimetable(5)
        viewModel.selectTimetable(TimetableViewModel.MY_TABLE_ID)

        createViewModel()

        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertNull(viewModel.selectedTimetableID.value)
        assertEquals("my", viewModel.selectedTimetable.value?.id)
    }

    @Test
    fun `refresh keeps the selected semester and timetable`() = runTest {
        val semesters = configureSelection()
        createViewModel()
        viewModel.selectPreviousSemester()
        viewModel.selectTimetable(5)

        viewModel.fetchData()

        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertEquals(5, viewModel.selectedTimetableID.value)
        assertEquals("5", viewModel.selectedTimetable.value?.id)
    }

    @Test
    fun `missing saved timetable falls back to My Table and persists fallback`() = runTest {
        val semesters = configureSelection()
        TimetableSelectionStore(context).save(semesters[1], 5)
        mockTimetableUseCase.getTimetableListResult = Result.success(emptyList())

        createViewModel()

        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertNull(viewModel.selectedTimetableID.value)
        assertEquals("my", viewModel.selectedTimetable.value?.id)
        assertNull(TimetableSelectionStore(context).selection?.timetableID)
    }

    @Test
    fun `unavailable semester falls back to current semester`() = runTest {
        val semesters = configureSelection()
        TimetableSelectionStore(context).save(semesters[1].copy(year = 1990), 5)

        createViewModel()

        assertEquals(semesters.last(), viewModel.selectedSemester.value)
        assertNull(viewModel.selectedTimetableID.value)
        assertEquals(semesters.last().id, TimetableSelectionStore(context).selection?.semesterID)
    }

    @Test
    fun `failed list fetch preserves saved choice for next restart`() = runTest {
        val semesters = configureSelection()
        TimetableSelectionStore(context).save(semesters[1], 5)
        mockTimetableUseCase.getTimetableListResult = Result.failure(Exception("Offline"))

        createViewModel()

        assertTrue(viewModel.showAlert)
        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertEquals(5, viewModel.selectedTimetableID.value)
        assertEquals(5, TimetableSelectionStore(context).selection?.timetableID)
        configureSelection()
        createViewModel()
        assertEquals("5", viewModel.selectedTimetable.value?.id)
    }

    @Test
    fun `failed semester fetch does not overwrite saved preference`() = runTest {
        val semesters = configureSelection()
        val store = TimetableSelectionStore(context)
        store.save(semesters[1], 5)
        mockTimetableUseCase.getSemestersResult = Result.failure(Exception("Offline"))

        createViewModel()

        assertEquals(TimetableSelectionStore.Selection(semesters[1].id, 5), store.selection)
    }

    @Test
    fun `semester navigation saves a consistent choice even when loading fails`() = runTest {
        val semesters = configureSelection()
        createViewModel()
        viewModel.selectTimetable(5)
        mockTimetableUseCase.getTimetableListResult = Result.failure(Exception("Offline"))

        viewModel.selectPreviousSemester()

        assertEquals(TimetableSelectionStore.Selection(semesters[1].id, null), TimetableSelectionStore(context).selection)
        assertNull(viewModel.selectedTimetableID.value)
        assertNull(viewModel.selectedTimetable.value)
    }

    @Test
    fun `creating and deleting selected table updates the restored choice`() = runTest {
        val semesters = configureSelection()
        val api = object : TimetableUseCaseProtocol by mockTimetableUseCase {
            override suspend fun createTable(semester: Semester) = TimetableCreation(5)
        }
        createViewModel(api)
        viewModel.selectPreviousSemester()
        viewModel.createTable()
        createViewModel()
        assertEquals(5, viewModel.selectedTimetableID.value)

        viewModel.deleteTable()
        createViewModel()

        assertEquals(semesters[1], viewModel.selectedSemester.value)
        assertNull(viewModel.selectedTimetableID.value)
        assertEquals("my", viewModel.selectedTimetable.value?.id)
    }

    @Test
    fun `late timetable response cannot replace a newer selection`() = runTest {
        configureSelection()
        val delayedTable = CompletableDeferred<Timetable>()
        val api = object : TimetableUseCaseProtocol by mockTimetableUseCase {
            override suspend fun getTable(id: Int, forceRefresh: Boolean) = delayedTable.await()
        }
        createViewModel(api)
        viewModel.selectTimetable(5)
        viewModel.selectTimetable(TimetableViewModel.MY_TABLE_ID)

        delayedTable.complete(Timetable("5", emptyList()))

        assertNull(viewModel.selectedTimetableID.value)
        assertEquals("my", viewModel.selectedTimetable.value?.id)
        assertNull(TimetableSelectionStore(context).selection?.timetableID)
    }

    @Test
    fun `late list refresh cannot clear a newer saved choice`() = runTest {
        configureSelection()
        val delayedList = CompletableDeferred<List<TimetableSummary>>()
        var delayRefresh = false
        val api = object : TimetableUseCaseProtocol by mockTimetableUseCase {
            override suspend fun getTimetableList(semester: Semester): List<TimetableSummary> =
                if (delayRefresh) delayedList.await() else mockTimetableUseCase.getTimetableList(semester)
        }
        createViewModel(api)
        delayRefresh = true
        viewModel.fetchData()
        viewModel.selectTimetable(5)

        delayedList.complete(emptyList())

        assertEquals(5, viewModel.selectedTimetableID.value)
        assertEquals("5", viewModel.selectedTimetable.value?.id)
        assertEquals(5, TimetableSelectionStore(context).selection?.timetableID)
    }

    @Test
    fun `clearing session preference starts with the default selection`() = runTest {
        val semesters = configureSelection()
        TimetableSelectionStore(context).save(semesters[1], 5)
        TimetableSelectionStore(context).clear()

        createViewModel()

        assertEquals(semesters.last(), viewModel.selectedSemester.value)
        assertNull(viewModel.selectedTimetableID.value)
    }

}
