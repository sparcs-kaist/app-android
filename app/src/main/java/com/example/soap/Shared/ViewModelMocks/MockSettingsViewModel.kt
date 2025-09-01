package com.example.soap.Shared.ViewModelMocks

import androidx.compose.runtime.mutableStateOf
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Features.Settings.SettingsViewModel
import com.example.soap.Features.Settings.SettingsViewModelProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockSettingsViewModel(initialState: SettingsViewModel.ViewState) : SettingsViewModelProtocol {

    // MARK: - Properties
    override var araAllowNSFWPosts = mutableStateOf(false)
    override var araAllowPoliticalPosts = mutableStateOf(false)
    override var araBlockedUsers = mutableStateOf(listOf("유능한 시조새_0b4c"))
    override var taxiBankName = mutableStateOf<String?>(null)
    override var taxiBankNumber = mutableStateOf("123-456-789")
    override var otlMajor = mutableStateOf("School of Computer Science")
    override val otlMajorList = listOf(
        "School of Computer Science",
        "School of Electrical Engineering",
        "School of Business"
    )

    override val taxiUser = MutableStateFlow<TaxiUser?>(null)

    private val _taxiState = MutableStateFlow(initialState)
    override val taxiState: StateFlow<SettingsViewModel.ViewState> = _taxiState

    // MARK: - Functions
    override suspend fun fetchTaxiUser() {}

    override suspend fun taxiEditBankAccount(account: String) {}
}