package org.sparcs.soap.App.Domain.Helpers

import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import javax.inject.Inject

interface UserStorageProtocol {

    suspend fun setAraUser(user: AraUser?)
    fun getAraUser(): AraUser?

    suspend fun setTaxiUser(user: TaxiUser?)
    fun getTaxiUser(): TaxiUser?

    suspend fun setFeedUser(user: FeedUser?)
    fun getFeedUser(): FeedUser?

    suspend fun setOTLUser(user: OTLUser?)
    fun getOTLUser(): OTLUser?
}

class UserStorage @Inject constructor(

) : UserStorageProtocol {
    @Volatile
    private var araUser: AraUser? = null
    @Volatile
    private var taxiUser: TaxiUser? = null
    @Volatile
    private var feedUser: FeedUser? = null
    @Volatile
    private var otlUser: OTLUser? = null

    private val lock = Any()

    //Mark: Ara
    override suspend fun setAraUser(user: AraUser?) {
        synchronized(lock) {
            araUser = user
        }
    }

    override fun getAraUser(): AraUser? {
        return synchronized(lock) { araUser }
    }

    //Mark: Taxi
    override suspend fun setTaxiUser(user: TaxiUser?) {
        synchronized(lock) { taxiUser = user }
    }

    override fun getTaxiUser(): TaxiUser? {
        return synchronized(lock) { taxiUser }
    }

    //Mark: Feed
    override suspend fun setFeedUser(user: FeedUser?) {
        synchronized(lock) { feedUser = user }
    }

    override fun getFeedUser(): FeedUser? {
        return synchronized(lock) { feedUser }
    }

    //MARK: OTL User
    override suspend fun setOTLUser(user: OTLUser?) {
        synchronized(lock) { otlUser = user }
    }

    override fun getOTLUser(): OTLUser? {
        return synchronized(lock) { otlUser }
    }
}