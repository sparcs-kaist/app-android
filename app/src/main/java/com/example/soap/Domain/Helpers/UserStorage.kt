package com.example.soap.Domain.Helpers

import com.example.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


object UserStorage : UserStorageProtocol {
    private var taxiUser: TaxiUser? = null
    private val mutex = Mutex()

    override suspend fun setTaxiUser(user: TaxiUser?) {
        mutex.withLock {
            taxiUser = user
        }
    }

    override suspend fun getTaxiUser(): TaxiUser? {
        return mutex.withLock { taxiUser }
    }
}