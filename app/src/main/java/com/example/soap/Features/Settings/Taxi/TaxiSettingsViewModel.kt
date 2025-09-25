package com.example.soap.Features.Settings.Taxi
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.TaxiUserRepository
import com.example.soap.Domain.Usecases.UserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editBankAccount(account: String)
}

@HiltViewModel
class TaxiSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val taxiUserRepository: TaxiUserRepository
) : ViewModel(), TaxiSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    override var bankName: String? = null
    override var bankNumber: String = ""
    override var user: TaxiUser? = null

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        viewModelScope.launch {
            val fetchedUser = userUseCase.getTaxiUser()
            if (fetchedUser == null) {
                _state.value = ViewState.Error("Taxi User Information Not Found.")
                return@launch
            }
            user = fetchedUser
            val parts = fetchedUser.account.split(" ")
            bankName = parts.firstOrNull()
            bankNumber = parts.getOrNull(1) ?: ""
            _state.value = ViewState.Loaded
        }
    }

    override suspend fun editBankAccount(account: String) {
        viewModelScope.launch {
            try {
                taxiUserRepository.editBankAccount(account)
                userUseCase.fetchTaxiUser()
            } catch (e: Exception) {
                Log.e("TaxiSettingsViewModel", "Failed to edit bank account", e)
            }
        }
    }
}
