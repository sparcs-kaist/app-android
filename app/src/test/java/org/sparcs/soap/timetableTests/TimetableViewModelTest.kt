package org.sparcs.soap.timetableTests

import android.content.Context
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
        mockTimetableUseCase = MockTimetableUseCase()
    }

    private fun createViewModel() {
        viewModel = TimetableViewModel(
            timetableUseCase = mockTimetableUseCase,
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
}
