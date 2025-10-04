package com.example.soap.Domain.Usecases

import android.util.Log
import com.example.soap.Domain.Helpers.UserStorageProtocol
import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Models.OTL.OTLUser
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Ara.AraUserRepositoryProtocol
import com.example.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.example.soap.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import com.example.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUseCase @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol,
    private val araUserRepository: AraUserRepositoryProtocol,
    private val feedUserRepository: FeedUserRepositoryProtocol,
    private val otlUserRepository: OTLUserRepositoryProtocol,
    private val userStorage: UserStorageProtocol
) : UserUseCaseProtocol {

    init {
        Log.d("UserUseCase","Fetching Users")

        CoroutineScope(Dispatchers.IO).launch {
            fetchUsers()
        }
    }

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchAraUser() {
        Log.d("UserUseCase","Fetching Ara User")
        val user = araUserRepository.fetchUser()
        userStorage.setAraUser(user)
        Log.d("UserUseCase", user.toString())
    }

    override suspend fun updateAraUser(params: Map<String, Any>) {
        Log.d("UserUseCase","Updating Ara User Information: $params")

        val araUser = araUser
        if (araUser == null) {
            Log.e("UserUseCase","Ara User Not Found")
            return
        }

        araUserRepository.updateMe(id = araUser.id, params = params)
        fetchAraUser()
    }

    override suspend fun fetchTaxiUser(): TaxiUser {
        Log.d("UserUseCase","Fetching Taxi User")
        val user = taxiUserRepository.fetchUser()
        userStorage.setTaxiUser(user)
        Log.d("UserUseCase", user.toString())
        return user
    }

    override suspend fun fetchFeedUser() {
        Log.d("UserUseCase","Fetching Feed User")
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
