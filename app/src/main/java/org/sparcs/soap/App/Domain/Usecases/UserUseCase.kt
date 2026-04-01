package org.sparcs.soap.App.Domain.Usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.sparcs.soap.App.Domain.Helpers.UserStorageProtocol
import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface UserUseCaseProtocol {

    val araUser: AraUser?
    val taxiUser: TaxiUser?
    val feedUser: FeedUser?
    val otlUser: OTLUser?

    suspend fun fetchUsers()

    @Throws(Exception::class)
    suspend fun fetchAraUser()

    @Throws(Exception::class)
    suspend fun updateAraUser(params: Map<String, Any>)

    @Throws(Exception::class)
    suspend fun fetchFeedUser()

    @Throws(Exception::class)
    suspend fun fetchTaxiUser()

    @Throws(Exception::class)
    suspend fun fetchOTLUser()
}

@Singleton
class UserUseCase @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol,
    private val araUserRepository: AraUserRepositoryProtocol,
    private val feedUserRepository: FeedUserRepositoryProtocol,
    private val otlUserRepository: OTLUserRepositoryProtocol,
    private val userStorage: UserStorageProtocol,
) : UserUseCaseProtocol {

    override val araUser: AraUser?
        get() = userStorage.getAraUser()

    override val taxiUser: TaxiUser?
        get() = userStorage.getTaxiUser()

    override val feedUser: FeedUser?
        get() = userStorage.getFeedUser()

    override val otlUser: OTLUser?
        get() = userStorage.getOTLUser()

    override suspend fun fetchUsers() {
        supervisorScope {
            val taxi = async { runCatching { fetchTaxiUser() } }
            val ara = async { runCatching { fetchAraUser() } }
            val feed = async { runCatching { fetchFeedUser() } }
            val otl = async { runCatching { fetchOTLUser() } }

            taxi.await()
            ara.await()
            feed.await()
            otl.await()
        }
    }

    override suspend fun fetchAraUser() {
        Timber.d("Fetching Ara User")
        val user = araUserRepository.fetchUser()
        userStorage.setAraUser(user)
    }

    override suspend fun updateAraUser(params: Map<String, Any>) {
        val currentId = araUser?.id ?: return
        araUserRepository.updateMe(id = currentId, params = params)
        fetchAraUser()
    }

    override suspend fun fetchTaxiUser() {
        val user = taxiUserRepository.fetchUser()
        userStorage.setTaxiUser(user)
    }

    override suspend fun fetchFeedUser() {
        val user = feedUserRepository.getUser()
        userStorage.setFeedUser(user)
    }

    override suspend fun fetchOTLUser() {
        val user = otlUserRepository.fetchUser()
        userStorage.setOTLUser(user)
    }
}

class MockUserUseCase : UserUseCaseProtocol {

    override var araUser: AraUser? = null
    override var taxiUser: TaxiUser? = null
    override var feedUser: FeedUser? = null
    override var otlUser: OTLUser? = null

    override suspend fun fetchUsers() {}
    override suspend fun fetchAraUser() {}
    override suspend fun updateAraUser(params: Map<String, Any>) {}
    override suspend fun fetchFeedUser() {}
    override suspend fun fetchTaxiUser() {}
    override suspend fun fetchOTLUser() {}
}
