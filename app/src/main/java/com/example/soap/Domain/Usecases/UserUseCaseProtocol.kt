package com.example.soap.Domain.Usecases

import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Models.Taxi.TaxiUser


interface UserUseCaseProtocol {

    val araUser: AraUser?
    val taxiUser: TaxiUser?
    val feedUser: FeedUser?

    suspend fun fetchUsers()

    @Throws(Exception::class)
    suspend fun fetchAraUser()

    @Throws(Exception::class)
    suspend fun updateAraUser(params: Map<String, Any>)

    @Throws(Exception::class)
    suspend fun fetchFeedUser()

    @Throws(Exception::class)
    suspend fun fetchTaxiUser()
}