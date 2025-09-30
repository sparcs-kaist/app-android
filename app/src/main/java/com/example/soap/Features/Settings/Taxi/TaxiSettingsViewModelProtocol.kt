package com.example.soap.Features.Settings.Taxi

import com.example.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.flow.StateFlow

interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editBankAccount(account: String)
}