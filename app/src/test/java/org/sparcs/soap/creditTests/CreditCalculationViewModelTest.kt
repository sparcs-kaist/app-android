package org.sparcs.soap.creditTests

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLTakenLecture
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureHistory
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.usecases.MockUserUseCase
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCase
import org.sparcs.soap.app.features.timetable.creditCalculation.CreditCalculationViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.app.shared.mocks.otl.mockList
import org.sparcs.soap.buddyTestSupport.useCase.MockLectureUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockTimetableUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class CreditCalculationViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val lectures = MockLectureUseCase()
    private val tables = MockTimetableUseCase()
    private val snapshots = mutableListOf<CreditSummarySnapshot>()
    private val publisher = object : CreditSummaryPublisher {
        override val revision = 0L
        override suspend fun publish(snapshot: CreditSummarySnapshot, expectedRevision: Long) {
            snapshots += snapshot
        }
    }
    private val user = MockUserUseCase().apply {
        otlUser = OTLUser(1, "Test", "", 20240000, "", emptyList(), emptyList())
    }

    @Before fun setup() {
        context.getSharedPreferences("lecture_grades", Context.MODE_PRIVATE).edit().clear().commit()
        val semester = Semester.mockList().first()
        lectures.historyResult = Result.success(OTLUserLectureHistory(listOf(
            OTLUserLectureSemester(semester.year, semester.semesterType,
                listOf(OTLTakenLecture(1, 1, "Test", "CS", emptyList(), false)))
        ), 1, 0, 0))
    }

    private fun viewModel() = CreditCalculationViewModel(lectures, tables, user, LectureGradeUseCase(context), publisher)

    @Test fun `loaded summary includes saved grades and every fetched semester`() = runTest {
        val table = Timetable.mock()
        val lecture = table.lectures.first()
        LectureGradeUseCase(context).setGrade(LectureGrade.A, lecture.id, 1)
        val state = viewModel().state.first { !it.isLoading }
        assertNull(state.error)
        assertEquals(1, state.semesters.size)
        assertEquals(table.lectures.size, state.overallSummary.lectureCount)
        assertEquals(LectureGrade.A, state.grades[lecture.id])
    }

    @Test fun `failed semester can be retried instead of showing a partial total`() = runTest {
        tables.getMyTableResult = Result.failure(IllegalStateException("offline"))
        val model = viewModel()
        assertNotNull(model.state.first { !it.isLoading }.error)
        tables.getMyTableResult = Result.success(Timetable.mock())
        model.load()
        val state = model.state.first { !it.isLoading && it.error == null }
        assertEquals(Timetable.mock().lectures.size, state.overallSummary.lectureCount)
    }

    @Test fun `successive grade selections persist the latest value and clearing removes it`() = runTest {
        val model = viewModel()
        val state = model.state.first { !it.isLoading }
        val lectureID = state.timetables.values.first().lectures.first().id
        model.setGrade(LectureGrade.A, lectureID)
        model.setGrade(LectureGrade.B, lectureID)
        model.state.first { it.grades[lectureID] == LectureGrade.B }
        assertEquals(LectureGrade.B, LectureGradeUseCase(context).grades(1)[lectureID])
        model.setGrade(null, lectureID)
        model.state.first { lectureID !in it.grades }
        assertFalse(LectureGradeUseCase(context).grades(1).containsKey(lectureID))
    }
}
