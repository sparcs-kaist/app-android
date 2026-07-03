package org.sparcs.soap.buddyPreviewSupport

import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol

class PreviewUserUseCase : UserUseCaseProtocol {
    override val araUser: AraUser? = null
    override val taxiUser: TaxiUser? = null
    override val feedUser: FeedUser = FeedUser(
        id = "preview",
        nickname = "PreviewUser",
        profileImageURL = null,
        karma = 42
    )
    override val otlUser: OTLUser? = null

    override suspend fun fetchUsers() {}
    override suspend fun fetchAraUser() {}
    override suspend fun fetchTaxiUser() {}
    override suspend fun fetchFeedUser() {}
    override suspend fun fetchOTLUser() {}
    override suspend fun updateAraUser(params: Map<String, Any>) {}
}