package org.sparcs.soap.BuddyPreviewSupport.Feed

import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol

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