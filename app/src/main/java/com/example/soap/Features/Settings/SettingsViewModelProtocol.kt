package com.example.soap.Features.Settings

import androidx.compose.runtime.MutableState
import com.example.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.flow.StateFlow

interface SettingsViewModelProtocol {

    // Taxi
    val taxiUser: StateFlow<TaxiUser?>
    val taxiState: StateFlow<SettingsViewModel.ViewState>
    var taxiBankName: MutableState<String?>
    var taxiBankNumber: MutableState<String>

    // Ara
    var araAllowNSFWPosts: MutableState<Boolean>
    var araAllowPoliticalPosts: MutableState<Boolean>
    var araBlockedUsers: MutableState<List<String>>

    // OTL
    var otlMajor: MutableState<String>
    val otlMajorList: List<String>

    // Suspend functions
    suspend fun fetchTaxiUser()
    suspend fun taxiEditBankAccount(account: String)

}
