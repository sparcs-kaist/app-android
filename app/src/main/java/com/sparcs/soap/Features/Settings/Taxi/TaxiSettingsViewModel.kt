package com.sparcs.soap.Features.Settings.Taxi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Models.Taxi.TaxiUser
import com.sparcs.soap.Domain.Repositories.Taxi.TaxiUserRepository
import com.sparcs.soap.Domain.Usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    var phoneNumber: String
    var residence: String
    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editBankAccount(account: String)
    suspend fun registerPhoneNumber(phoneNumber: String)
    suspend fun registerResidence(residence: String)
}

@HiltViewModel
class TaxiSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val taxiUserRepository: TaxiUserRepository,
) : ViewModel(), TaxiSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    override var bankName by mutableStateOf<String?>(null)
    override var bankNumber by mutableStateOf("")

    override var phoneNumber by mutableStateOf("")
    override var residence by mutableStateOf("")

    override var user: TaxiUser? = null

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        viewModelScope.launch {
            val fetchedUser = userUseCase.taxiUser
            if (fetchedUser == null) {
                _state.value = ViewState.Error("Taxi User Information Not Found.")
                return@launch
            }
            user = fetchedUser
            val parts = fetchedUser.account.split(" ")
            bankName = parts.firstOrNull()
            bankNumber = parts.getOrNull(1) ?: ""
            phoneNumber = fetchedUser.phoneNumber ?: ""
            residence = fetchedUser.residence ?: ""
            _state.value = ViewState.Loaded
        }
    }

    override suspend fun editBankAccount(account: String) {
        taxiUserRepository.editBankAccount(account)
        userUseCase.fetchTaxiUser()
    }

    override suspend fun registerPhoneNumber(phoneNumber: String) {
        taxiUserRepository.registerPhoneNumber(phoneNumber)
        userUseCase.fetchTaxiUser()
    }

    override suspend fun registerResidence(residence: String) {
        taxiUserRepository.registerResidence(residence)
        userUseCase.fetchTaxiUser()
    }
}
