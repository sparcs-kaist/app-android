package org.sparcs.soap.taxiRoomCreationTests

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.features.taxiRoomCreation.TaxiRoomCreationViewModel
import org.sparcs.soap.buddyTestSupport.useCase.MockTaxiRoomUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule

class TaxiRoomCreationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockTaxiRoomUseCase: MockTaxiRoomUseCase
    private lateinit var viewModel: TaxiRoomCreationViewModel

    @Before
    fun setup() {
        mockTaxiRoomUseCase = MockTaxiRoomUseCase()
        viewModel = TaxiRoomCreationViewModel(mockTaxiRoomUseCase)
    }

    @Test
    fun `initial block status is allow`() {
        assertEquals(TaxiRoomBlockStatus.Allow, viewModel.blockStatus.value)
    }

    @Test
    fun `fetchBlockStatus reflects not paid`() = runTest {
        mockTaxiRoomUseCase.isBlockedResult = TaxiRoomBlockStatus.NotPaid

        viewModel.fetchBlockStatus()

        assertEquals(TaxiRoomBlockStatus.NotPaid, viewModel.blockStatus.value)
        assertEquals(1, mockTaxiRoomUseCase.isBlockedCallCount)
    }

    @Test
    fun `fetchBlockStatus reflects too many rooms`() = runTest {
        mockTaxiRoomUseCase.isBlockedResult = TaxiRoomBlockStatus.TooManyRooms

        viewModel.fetchBlockStatus()

        assertEquals(TaxiRoomBlockStatus.TooManyRooms, viewModel.blockStatus.value)
    }
}
