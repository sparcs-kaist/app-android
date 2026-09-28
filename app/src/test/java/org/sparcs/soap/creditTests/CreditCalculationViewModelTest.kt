package org.sparcs.soap.creditTests

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.first
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
import org.robolectric.annotation.Config
import org.sparcs.soap.app.domain.helpers.CreditSummaryPublisher
import org.sparcs.soap.app.domain.models.otl.CreditSummarySnapshot
import org.sparcs.soap.app.domain.models.otl.CreditRequirements
import org.sparcs.soap.app.domain.models.otl.LectureGrade
import org.sparcs.soap.app.domain.models.otl.OTLTakenLecture
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureHistory
import org.sparcs.soap.app.domain.models.otl.OTLUserLectureSemester
import org.sparcs.soap.app.domain.models.otl.Semester
import org.sparcs.soap.app.domain.models.otl.Timetable
import org.sparcs.soap.app.domain.usecases.MockUserUseCase
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCase
import org.sparcs.soap.app.domain.usecases.otl.LectureGradeUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.TimetableUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
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
        override var revision = 0L
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

    private fun viewModel(
        grades: LectureGradeUseCaseProtocol = LectureGradeUseCase(context),
        timetable: TimetableUseCaseProtocol = tables,
        users: UserUseCaseProtocol = user,
    ) = CreditCalculationViewModel(lectures, timetable, users, grades, publisher)

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

    @Test fun `failed consecutive saves leave the last committed grade and widget intact`() = runTest {
        val store = GradeStore()
        val model = viewModel(store)
        val id = model.state.value.timetables.values.first().lectures.first().id
        model.setGrade(LectureGrade.A, id)
        val committed = snapshots.last()
        store.failure = true
        model.setGrade(LectureGrade.B, id)
        model.setGrade(LectureGrade.C, id)
        assertEquals(LectureGrade.A, model.state.value.grades[id])
        assertEquals(LectureGrade.A, store.values[id])
        assertEquals(committed, snapshots.last())
        assertTrue(model.state.value.saveError)
        store.failure = false
        model.setGrade(null, id)
        assertNull(model.state.value.grades[id])
        assertNull(store.values[id])
    }

    @Test fun `sign out during initial user fetch cannot adopt the next session`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val delayedUser = object : UserUseCaseProtocol by user {
            var fetched = false
            override val otlUser get() = if (fetched) user.otlUser else null
            override suspend fun fetchOTLUser() { gate.await(); fetched = true }
        }
        val model = viewModel(GradeStore(), users = delayedUser)
        publisher.revision++
        gate.complete(Unit)
        model.refresh()
        assertTrue(snapshots.isEmpty())
        assertTrue(model.state.value.timetables.isEmpty())
    }

    @Test fun `sign out and same account sign in invalidate queued and in flight saves`() = runTest {
        val store = GradeStore()
        val model = viewModel(store)
        val id = model.state.value.timetables.values.first().lectures.first().id
        val gate = CompletableDeferred<Unit>()
        store.saveGate = gate
        model.setGrade(LectureGrade.A, id)
        model.setGrade(LectureGrade.B, id)
        val published = snapshots.size
        publisher.revision++
        gate.complete(Unit)
        model.refresh()
        assertEquals(1, store.saveCalls)
        assertEquals(published, snapshots.size)
        assertNull(model.state.value.grades[id])
    }

    @Test fun `reload while a grade is saved keeps the committed grade`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var waitForReload = false
        val delayedTables = object : TimetableUseCaseProtocol by tables {
            override suspend fun getMyTable(semester: Semester, forceRefresh: Boolean): Timetable {
                if (waitForReload) gate.await()
                return tables.getMyTable(semester, forceRefresh)
            }
        }
        val store = GradeStore()
        val model = viewModel(store, delayedTables)
        val id = model.state.value.timetables.values.first().lectures.first().id
        waitForReload = true
        model.load()
        model.setGrade(LectureGrade.B_PLUS, id)
        gate.complete(Unit)
        assertEquals(LectureGrade.B_PLUS, model.state.value.grades[id])
        assertEquals(store.values, model.state.value.grades)
    }

    @Test fun `unavailable historical semester does not publish a partial widget total`() = runTest {
        tables.getSemestersResult = Result.success(emptyList())
        val state = viewModel(GradeStore()).state.value
        assertFalse(state.isLoading)
        assertTrue(state.timetables.isEmpty())
        assertTrue(snapshots.isEmpty())
    }

    @Test fun `account change during requirement save does not update old screen or publish`() = runTest {
        val store = GradeStore()
        val model = viewModel(store)
        val before = model.state.value.requirements
        val gate = CompletableDeferred<Unit>()
        store.saveGate = gate
        model.updateRequirements(before.copy(graduation = 150))
        val published = snapshots.size
        user.otlUser = user.otlUser!!.copy(id = 2)
        gate.complete(Unit)
        assertEquals(before, model.state.value.requirements)
        assertEquals(published, snapshots.size)
    }

    private class GradeStore : LectureGradeUseCaseProtocol {
        val values = mutableMapOf<Int, LectureGrade>()
        var failure = false
        var saveGate: CompletableDeferred<Unit>? = null
        var saveCalls = 0
        override suspend fun grades(userID: Int) = values.toMap()
        override suspend fun requirements(userID: Int) = CreditRequirements()
        override suspend fun setGrade(grade: LectureGrade?, lectureID: Int, userID: Int) {
            saveCalls++
            saveGate?.await()
            if (failure) throw java.io.IOException("Save failed")
            if (grade == null) values.remove(lectureID) else values[lectureID] = grade
        }
        override suspend fun saveRequirements(requirements: CreditRequirements, userID: Int) {
            saveGate?.await()
            if (failure) throw java.io.IOException("Save failed")
        }
    }
}
