package org.sparcs.soap.App.Domain.Usecases

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.sparcs.soap.App.Domain.Helpers.UserStorageProtocol
import org.sparcs.soap.App.Domain.Models.Ara.AraUser
import org.sparcs.soap.App.Domain.Models.Feed.FeedUser
import org.sparcs.soap.App.Domain.Models.OTL.OTLUser
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepositoryProtocol
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
    private val otlUserRepository: org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepositoryProtocol,
    private val userStorage: UserStorageProtocol,
) : UserUseCaseProtocol {

    override var araUser: AraUser? by mutableStateOf(null)
        private set
    override var taxiUser: TaxiUser? by mutableStateOf(null)
        private set
    override var feedUser: FeedUser? by mutableStateOf(null)
        private set
    override var otlUser: OTLUser? by mutableStateOf(null)
        private set

    override suspend fun fetchUsers() {
        supervisorScope {
            val taxi = async { runCatching { fetchTaxiUser() } }
            val ara = async { runCatching { fetchAraUser() } }
            val feed = async { runCatching { fetchFeedUser() } }
            val otl = async { runCatching { fetchOTLUser() } }

            val results = listOf(taxi.await(), ara.await(), feed.await(), otl.await())

            if (results.all { it.isFailure }) {
                val firstError = results.firstNotNullOfOrNull { it.exceptionOrNull() }
                Timber.e(firstError, "All fetches failed")
            } else {
                results.forEachIndexed { index, result ->
                    if (result.isFailure) {
                        Timber.w("Fetch task $index failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        }
    }

    override suspend fun fetchAraUser() {
        Timber.d("Fetching Ara User")
        val user = araUserRepository.fetchUser()
        userStorage.setAraUser(user)
        araUser = user
    }

    override suspend fun updateAraUser(params: Map<String, Any>) {
        val currentId = araUser?.id ?: return
        araUserRepository.updateMe(id = currentId, params = params)
        fetchAraUser()
    }

    override suspend fun fetchTaxiUser() {
        val user = taxiUserRepository.fetchUser()
        userStorage.setTaxiUser(user)
        taxiUser = user
    }

    override suspend fun fetchFeedUser() {
        val user = feedUserRepository.getUser()
        userStorage.setFeedUser(user)
        feedUser = user
    }

    override suspend fun fetchOTLUser() {
        val user = otlUserRepository.fetchUser()
        userStorage.setOTLUser(user)
        otlUser = user
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
