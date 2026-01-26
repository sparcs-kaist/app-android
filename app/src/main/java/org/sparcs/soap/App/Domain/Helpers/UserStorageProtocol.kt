package org.sparcs.soap.App.Domain.Helpers

import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser

interface UserStorageProtocol {

    suspend fun setAraUser(user: AraUser?)
    suspend fun getAraUser(): AraUser?

    suspend fun setTaxiUser(user: TaxiUser?)
    suspend fun getTaxiUser(): TaxiUser?

    suspend fun setFeedUser(user: FeedUser?)
    suspend fun getFeedUser(): FeedUser?

    suspend fun setOTLUser(user: OTLUser?)
    suspend fun getOTLUser(): OTLUser?
}