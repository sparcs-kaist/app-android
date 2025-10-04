package com.example.soap.Domain.Usecases

import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Models.OTL.OTLUser
import com.example.soap.Domain.Models.Taxi.TaxiUser


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

    suspend fun fetchTaxiUser(): TaxiUser

    @Throws(Exception::class)
    suspend fun fetchOTLUser()
}