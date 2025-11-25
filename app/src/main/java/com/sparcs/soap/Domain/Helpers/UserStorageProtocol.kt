package com.sparcs.soap.Domain.Helpers

import com.sparcs.soap.Domain.Models.Ara.AraUser
import com.sparcs.soap.Domain.Models.Feed.FeedUser
import com.sparcs.soap.Domain.Models.OTL.OTLUser
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser

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