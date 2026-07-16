package org.sparcs.soap.taxiReportTests

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.features.taxiReport.TaxiReportViewModel
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.buddyTestSupport.MockCrashlyticsService
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiReportRepository
import org.sparcs.soap.testSupport.MainDispatcherRule

class TaxiReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTaxiReportRepository: MockTaxiReportRepository
    private lateinit var viewModel: TaxiReportViewModel
    private val room: TaxiRoom = TaxiRoom.mock()

    @Before
    fun setup() {
        mockTaxiReportRepository = MockTaxiReportRepository()
    }

    private fun createViewModel(withRoom: TaxiRoom = room) {
        val savedStateHandle = SavedStateHandle(mapOf("room_json" to Gson().toJson(withRoom)))
        viewModel = TaxiReportViewModel(
            savedStateHandle = savedStateHandle,
            taxiReportRepository = mockTaxiReportRepository,
            crashlyticsService = MockCrashlyticsService(),
        )
    }

    @Test
    fun `room is decoded from room_json`() {
        createViewModel()
        assertEquals(room.id, viewModel.room.value.id)
    }

    @Test
    fun `createReport without a selected user returns false`() = runTest {
        createViewModel()

        val result = viewModel.createReport(roomID = room.id)

        assertFalse(result)
        assertNull(viewModel.alertState)
    }

    @Test
    fun `createReport success presents success alert and returns true`() = runTest {
        createViewModel()
        viewModel.setSelectedUser(room.participants.first())

        val result = viewModel.createReport(roomID = room.id)

        assertTrue(result)
        assertTrue(viewModel.isAlertPresented)
        assertNotNull(viewModel.alertState)
    }

    @Test
    fun `createReport failure presents error alert and returns false`() = runTest {
        mockTaxiReportRepository.createReportResult = Result.failure(Exception("Test failure"))
        createViewModel()
        viewModel.setSelectedUser(room.participants.first())

        val result = viewModel.createReport(roomID = room.id)

        assertFalse(result)
        assertTrue(viewModel.isAlertPresented)
        assertNotNull(viewModel.alertState)
    }
}
