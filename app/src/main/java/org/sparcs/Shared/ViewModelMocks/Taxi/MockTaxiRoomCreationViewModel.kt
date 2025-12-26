package org.sparcs.Shared.ViewModelMocks.Taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.Domain.Enums.Taxi.TaxiRoomBlockStatus
import org.sparcs.Features.TaxiRoomCreation.TaxiRoomCreationViewModelProtocol

class MockTaxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol {
    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override fun fetchBlockStatus() {}
}