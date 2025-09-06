package com.example.soap.Domain.Usecases

import android.util.Log
import com.example.soap.Domain.Helpers.UserStorageProtocol
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUseCase @Inject constructor(
    private val taxiUserRepository: TaxiUserRepositoryProtocol,
    private val userStorage: UserStorageProtocol
) : UserUseCaseProtocol {

    init {
        Log.d("UserUseCase","Fetching Users")

        GlobalScope.launch {
            fetchUsers()
        }
    }

    override val taxiUser: TaxiUser?
        get() = runBlocking { userStorage.getTaxiUser() }

    override suspend fun fetchUsers() {
        try {
            fetchTaxiUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun fetchTaxiUser() {
        Log.d("UserUseCase","Fetching Taxi User")
        val user = taxiUserRepository.fetchUser()
        userStorage.setTaxiUser(user)
        Log.d("UserUseCase", user.toString())
    }
}
