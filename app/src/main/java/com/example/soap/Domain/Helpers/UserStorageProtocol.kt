package com.example.soap.Domain.Helpers

import com.example.soap.Domain.Models.Taxi.TaxiUser

interface UserStorageProtocol {
    suspend fun setTaxiUser(user: TaxiUser?)
    suspend fun getTaxiUser(): TaxiUser?
}