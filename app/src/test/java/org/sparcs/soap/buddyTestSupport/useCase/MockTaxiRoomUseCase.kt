package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.enums.taxi.TaxiRoomBlockStatus
import org.sparcs.soap.app.domain.usecases.taxi.TaxiRoomUseCaseProtocol

class MockTaxiRoomUseCase : TaxiRoomUseCaseProtocol {

    var isBlockedResult: TaxiRoomBlockStatus = TaxiRoomBlockStatus.Allow
    var isBlockedCallCount = 0

    override suspend fun isBlocked(): TaxiRoomBlockStatus {
        isBlockedCallCount += 1
        return isBlockedResult
    }
}
