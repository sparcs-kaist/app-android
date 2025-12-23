package com.sparcs.soap.Domain.Usecases

import android.util.Log
import com.sparcs.soap.Domain.Helpers.UserStorageProtocol
import com.sparcs.soap.Domain.Models.Ara.AraUser
import com.sparcs.soap.Domain.Models.Feed.FeedUser
import com.sparcs.soap.Domain.Models.OTL.OTLUser
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Domain.Repositories.Ara.AraUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import kotlinx.coroutines.runBlocking
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
        get() = runBlocking { userStorage.getAraUser() }

    override val feedUser: FeedUser?
        get() = runBlocking { userStorage.getFeedUser() }

    override val taxiUser: TaxiUser?
        get() = runBlocking { userStorage.getTaxiUser() }

    override val otlUser: OTLUser?
        get() = runBlocking { userStorage.getOTLUser() }

    override suspend fun fetchUsers() {
        try {
            fetchTaxiUser()
            fetchAraUser()
            fetchFeedUser()
            fetchOTLUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchAraUser() {
        Log.d("UserUseCase", "Fetching Ara User")
        val user = araUserRepository.fetchUser()
        userStorage.setAraUser(user)
        Log.d("UserUseCase", user.toString())
    }

    override suspend fun updateAraUser(params: Map<String, Any>) {
        Log.d("UserUseCase", "Updating Ara User Information: $params")

        val araUser = araUser
        if (araUser == null) {
            Log.e("UserUseCase", "Ara User Not Found")
            return
        }

        araUserRepository.updateMe(id = araUser.id, params = params)
        fetchAraUser()
    }

    override suspend fun fetchTaxiUser() {
        Log.d("UserUseCase", "Fetching Taxi User")
        val user = taxiUserRepository.fetchUser()
        userStorage.setTaxiUser(user)
        Log.d("UserUseCase", user.toString())
    }

    override suspend fun fetchFeedUser() {
        Log.d("UserUseCase", "Fetching Feed User")
        val user = feedUserRepository.getUser()
        userStorage.setFeedUser(user)
        Log.d("UserUseCase", user.toString())
    }

    override suspend fun fetchOTLUser() {
        Log.d("UserUseCase", "Fetching OTL User")
        val user = otlUserRepository.fetchUser()
        userStorage.setOTLUser(user)
    }
}

class MockUserUseCase: UserUseCaseProtocol {

    override var araUser: AraUser? = null
    override var taxiUser: TaxiUser? = null
    override var feedUser: FeedUser? = null
    override var otlUser: OTLUser? = null

    override suspend fun fetchUsers() {}
    override suspend fun fetchAraUser() {}
    override suspend fun updateAraUser(params: Map<String, Any>) { }
    override suspend fun fetchFeedUser() {}
    override suspend fun fetchTaxiUser() {}

    override suspend fun fetchOTLUser() {}
}
