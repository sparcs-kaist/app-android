package com.sparcs.soap.Shared.ViewModelMocks.Taxi

import com.sparcs.soap.Domain.Enums.Taxi.TaxiRoomBlockStatus
import com.sparcs.soap.Features.TaxiRoomCreation.TaxiRoomCreationViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockTaxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol {
    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override fun fetchBlockStatus() {}
}