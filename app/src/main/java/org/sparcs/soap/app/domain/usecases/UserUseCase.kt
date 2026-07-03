package org.sparcs.soap.app.domain.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.sparcs.soap.app.domain.helpers.UserStorageProtocol
import org.sparcs.soap.app.domain.models.ara.AraUser
import org.sparcs.soap.app.domain.models.feed.FeedUser
import org.sparcs.soap.app.domain.models.otl.OTLUser
import org.sparcs.soap.app.domain.models.taxi.TaxiUser
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiUserRepositoryProtocol
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

            taxi.await().onFailure { throwable ->
                Timber.e(throwable, "Failed to fetch Taxi user in fetchUsers()")
            }
            ara.await().onFailure { throwable ->
                Timber.e(throwable, "Failed to fetch Ara user in fetchUsers()")
            }
            feed.await().onFailure { throwable ->
                Timber.e(throwable, "Failed to fetch Feed user in fetchUsers()")
            }
            otl.await().onFailure { throwable ->
                Timber.e(throwable, "Failed to fetch OTL user in fetchUsers()")
            }
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
