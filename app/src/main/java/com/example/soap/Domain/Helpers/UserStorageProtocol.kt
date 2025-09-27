package com.example.soap.Domain.Helpers

import com.example.soap.Domain.Models.Ara.AraUser
import com.example.soap.Domain.Models.Feed.FeedUser
import com.example.soap.Domain.Models.Taxi.TaxiUser

interface UserStorageProtocol {

    suspend fun setAraUser(user: AraUser?)
    suspend fun getAraUser(): AraUser?

    suspend fun setTaxiUser(user: TaxiUser?)
    suspend fun getTaxiUser(): TaxiUser?

    suspend fun setFeedUser(user: FeedUser?)
    suspend fun getFeedUser(): FeedUser?
}