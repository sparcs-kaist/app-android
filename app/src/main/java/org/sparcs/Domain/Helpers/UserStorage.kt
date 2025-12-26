package org.sparcs.Domain.Helpers

import org.sparcs.Domain.Models.Ara.AraUser
import org.sparcs.Domain.Models.Feed.FeedUser
import org.sparcs.Domain.Models.OTL.OTLUser
import org.sparcs.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject


class UserStorage @Inject constructor(

): UserStorageProtocol {
    private var araUser: AraUser? = null
    private var taxiUser: TaxiUser? = null
    private var feedUser: FeedUser? = null
    private val mutex = Mutex()

    //Mark: Ara
    override suspend fun setAraUser(user: AraUser?) {
        mutex.withLock {
            araUser = user
        }
    }
    override suspend fun getAraUser(): AraUser? {
        return mutex.withLock { araUser }
    }

    //Mark: Taxi
    override suspend fun setTaxiUser(user: TaxiUser?) {
        mutex.withLock {
            taxiUser = user
        }
    }
    override suspend fun getTaxiUser(): TaxiUser? {
        return mutex.withLock { taxiUser }
    }

    //Mark: Feed
    override suspend fun setFeedUser(user: FeedUser?) {
        mutex.withLock {
            feedUser = user
        }
    }
    override suspend fun getFeedUser(): FeedUser? {
        return mutex.withLock { feedUser }
    }

    //MARK: OTL User
    private var otlUser: OTLUser? = null

    override suspend fun setOTLUser(user: OTLUser?) {
        mutex.withLock {
            otlUser = user
        }
    }

    override suspend fun getOTLUser(): OTLUser? {
        return mutex.withLock { otlUser }
    }
}