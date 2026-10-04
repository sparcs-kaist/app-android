package org.sparcs.soap.lectureSearchTests

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.otl.Department
import org.sparcs.soap.app.domain.models.otl.DepartmentOption
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.otl.LectureUseCaseProtocol
import org.sparcs.soap.app.features.settings.timetable.TimetableSettingsViewModel
import org.sparcs.soap.app.shared.mocks.otl.mock
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.useCase.MockLectureUseCase
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableSettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val user = MockUserUseCase()
    private val lectures = object : LectureUseCaseProtocol by MockLectureUseCase() {
        override suspend fun fetchDepartmentOptions() = listOf(DepartmentOption(1, "CS", "CS"), DepartmentOption(2, "Math", "MAS"))
    }

    @Test
    fun `loads saved selection and saves empty selection`() = runTest {
        user.otlUser = OTLUser.mock().copy(interestedDepartments = listOf(Department(1, "CS")))
        var saved: List<Int>? = null
        val users = object : UserUseCaseProtocol by user {
            override suspend fun updateInterestedDepartments(departmentIDs: List<Int>) { saved = departmentIDs }
        }
        val vm = TimetableSettingsViewModel(users, lectures, MockCrashlyticsService())
        vm.load()
        val state1 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertEquals(setOf(1), state1.selectedDepartmentIDs)
        assertFalse(state1.hasChanges)
        vm.selectDepartments(emptySet())
        var dismissed = false
        vm.save { dismissed = true }
        val state2 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertEquals(emptyList<Int>(), saved)
        assertTrue(dismissed)
        assertFalse(state2.hasChanges)
    }

    @Test
    fun `failed save retains selection and supports retry`() = runTest {
        var fail = true
        val users = object : UserUseCaseProtocol by user {
            override suspend fun updateInterestedDepartments(departmentIDs: List<Int>) { if (fail) error("offline") }
        }
        val vm = TimetableSettingsViewModel(users, lectures, MockCrashlyticsService())
        vm.load()
        vm.selectDepartments(setOf(2))
        var dismissed = false
        vm.save { dismissed = true }
        val state1 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertFalse(dismissed)
        assertTrue(state1.hasChanges)
        assertEquals(setOf(2), state1.selectedDepartmentIDs)
        assertFalse(state1.isSaving)
        fail = false
        vm.save { dismissed = true }
        val state2 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertTrue(dismissed)
        assertFalse(state2.hasChanges)
    }

    @Test
    fun `saving prevents a second request or changed selection`() = runTest {
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val users = object : UserUseCaseProtocol by user {
            override suspend fun updateInterestedDepartments(departmentIDs: List<Int>) { calls++; gate.await() }
        }
        val vm = TimetableSettingsViewModel(users, lectures, MockCrashlyticsService())
        vm.load()
        vm.selectDepartments(setOf(1))
        vm.save {}
        vm.selectDepartments(setOf(2))
        vm.save {}
        val state1 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertEquals(1, calls)
        assertEquals(setOf(1), state1.selectedDepartmentIDs)
        gate.complete(Unit)
        runCurrent()
        val state2 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertFalse(state2.isSaving)
    }

    @Test
    fun `picker remains usable if interested departments cannot be fetched`() = runTest {
        val users = object : UserUseCaseProtocol by user {
            override suspend fun fetchOTLUser() { error("user unavailable") }
        }
        val vm = TimetableSettingsViewModel(users, lectures, MockCrashlyticsService())
        vm.fetchDepartments()
        val state1 = vm.state.value as TimetableSettingsViewModel.ViewState.Loaded
        assertEquals(2, state1.departments.size)
        vm.load()
        assertTrue(vm.state.value is TimetableSettingsViewModel.ViewState.Error)
    }
}
