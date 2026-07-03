package org.sparcs.soap.app.shared.viewModelMocks.taxi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.features.taxiRoomCreation.TaxiRoomCreationViewModelProtocol

class MockTaxiRoomCreationViewModel: TaxiRoomCreationViewModelProtocol {
    private val _blockStatus = MutableStateFlow<TaxiRoomBlockStatus>(TaxiRoomBlockStatus.Allow)
    override val blockStatus: StateFlow<TaxiRoomBlockStatus> = _blockStatus

    override fun fetchBlockStatus() {}
}