package org.sparcs.soap.timetableTests

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.error.NetworkError
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.models.otl.TimetableCachedState
import org.sparcs.soap.app.domain.models.otl.TimetableSummary
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.useCase.MockTimetableUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule
import org.sparcs.soap.widgets.buddyTimetableWidget.TimetableWidgetConfigViewModel

class TimetableWidgetConfigViewModelTest {
    @get:Rule
    val dispatcher = MainDispatcherRule()
    private val semester = Semester.mockList().first()
    private val mock = MockTimetableUseCase().apply {
        getTableResult = Result.success(Timetable("44", emptyList()))
    }

    @Test
    fun offlineListPreservesSavedSelectionAndLoadsCachedTable() = runTest {
        val api = object : TimetableUseCaseProtocol by mock {
            override suspend fun cachedState(semester: Semester?, timetableID: Int?) =
                TimetableCachedState(
                    timetables = listOf(
                        TimetableSummary(
                            44,
                            "Saved",
                            this@TimetableWidgetConfigViewModelTest.semester.year,
                            this@TimetableWidgetConfigViewModelTest.semester.semesterType
                        )
                    )
                )

            override suspend fun refreshTimetableList(semester: Semester): List<TimetableSummary> =
                throw NetworkError.NoConnection()
        }
        val model = TimetableWidgetConfigViewModel(api)
        model.initialize(44, semester.year, semester.semesterType.intValue)
        assertEquals(44, model.state.value.selectedTimetableId)
        assertEquals("Saved", model.state.value.timetableList.single().title)
        assertEquals("44", model.state.value.selectedTimetable?.id)
    }

    @Test
    fun successfulEmptyListResetsDeletedSelection() = runTest {
        val model = TimetableWidgetConfigViewModel(mock)
        model.initialize(44, semester.year, semester.semesterType.intValue)
        assertEquals(-1, model.state.value.selectedTimetableId)
    }

    @Test
    fun switchingTablesClearsOldPreviewUntilNewRequestCompletes() = runTest {
        val delayed = CompletableDeferred<Timetable>()
        val api = object : TimetableUseCaseProtocol by mock {
            override suspend fun refreshTimetableList(semester: Semester): List<TimetableSummary> =
                throw NetworkError.NoConnection()

            override suspend fun getTable(id: Int, forceRefresh: Boolean) =
                if (id == 45) delayed.await() else Timetable(id.toString(), emptyList())
        }
        val model = TimetableWidgetConfigViewModel(api)
        model.initialize(44, semester.year, semester.semesterType.intValue)
        model.selectTimetable(45)
        assertNull(model.state.value.selectedTimetable)
        model.selectTimetable(46)
        delayed.complete(Timetable("45", emptyList()))
        assertEquals("46", model.state.value.selectedTimetable?.id)
    }
}
