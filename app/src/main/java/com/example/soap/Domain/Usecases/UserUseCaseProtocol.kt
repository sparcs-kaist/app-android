package com.example.soap.Domain.Usecases

import com.example.soap.Domain.Models.Taxi.TaxiUser


interface UserUseCaseProtocol {
    val taxiUser: TaxiUser?

    suspend fun fetchUsers()

    @Throws(Exception::class)
    suspend fun fetchTaxiUser()
}