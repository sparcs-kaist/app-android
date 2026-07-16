package org.sparcs.soap.buddyTestSupport.useCase

import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol

class MockUserUseCase : UserUseCaseProtocol {

    override var araUser: AraUser? = null
    override var taxiUser: TaxiUser? = null
    override var feedUser: FeedUser? = null
    override var otlUser: OTLUser? = null

    var fetchUsersResult: Result<Unit> = Result.success(Unit)
    var fetchUsersCallCount = 0

    override suspend fun fetchUsers() {
        fetchUsersCallCount += 1
        fetchUsersResult.getOrThrow()
    }

    override suspend fun fetchAraUser() {}
    override suspend fun updateAraUser(params: Map<String, Any>) {}
    override suspend fun fetchFeedUser() {}
    override suspend fun fetchTaxiUser() {}
    override suspend fun fetchOTLUser() {}
}
