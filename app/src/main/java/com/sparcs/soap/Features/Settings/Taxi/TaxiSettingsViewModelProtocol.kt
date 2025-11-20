package com.sparcs.soap.Features.Settings.Taxi

import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.flow.StateFlow

interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editBankAccount(account: String)
}