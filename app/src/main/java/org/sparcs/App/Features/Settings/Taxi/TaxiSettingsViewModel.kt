package org.sparcs.App.Features.Settings.Taxi

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.App.Domain.Helpers.CrashlyticsHelper
import org.sparcs.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.App.Domain.Repositories.Taxi.TaxiUserRepository
import org.sparcs.App.Domain.Usecases.UserUseCase
import javax.inject.Inject

interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    var phoneNumber: String
    var showBadge: Boolean
    var residence: String
    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editInformation()
}

@HiltViewModel
class TaxiSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCase,
    private val taxiUserRepository: TaxiUserRepository,
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel(), TaxiSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val message: String) : ViewState()
    }

    override var bankName by mutableStateOf<String?>(null)
    override var bankNumber by mutableStateOf("")

    override var phoneNumber by mutableStateOf("")
    override var showBadge by mutableStateOf(false)
    override var residence by mutableStateOf("")
    override var user by mutableStateOf<TaxiUser?>(null)

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        try {
            val fetchedUser = userUseCase.taxiUser
            if (fetchedUser == null) {
                _state.value = ViewState.Error("Taxi User Information Not Found.")
                return
            }
            user = fetchedUser
            val parts = fetchedUser.account.split(" ")
            bankName = parts.firstOrNull()
            bankNumber = parts.getOrNull(1) ?: ""
            phoneNumber = fetchedUser.phoneNumber ?: ""
            showBadge = fetchedUser.badge ?: false
            residence = fetchedUser.residence ?: ""
            _state.value = ViewState.Loaded
        } catch (e: Exception) {
            _state.value = ViewState.Error(e.message ?: "Unknown Error")
        }
    }


    override suspend fun editInformation() {
        try {
            bankName?.let { name ->
                if (name.isNotEmpty() && bankNumber.isNotEmpty()) {
                    editBankAccount(name, bankNumber)
                }
            }
            if (phoneNumber.isNotEmpty() && user?.phoneNumber != phoneNumber) {
                registerPhoneNumber(phoneNumber)
            }
            if (user?.badge != showBadge) {
                editBadge(showBadge)
            }
            if (user?.residence != residence) {
                registerResidence(residence)
            }
            userUseCase.fetchTaxiUser()
        } catch (e: Exception) {
            Log.d("TaxiSettingsViewModel", "Error editing information: $e")
            crashlyticsHelper.recordException(e)
        }
    }

    private suspend fun editBankAccount(bankName: String, bankNumber: String) {
        try {
            taxiUserRepository.editBankAccount(account = "$bankName $bankNumber")
        } catch (e: Exception) {
            Log.d("TaxiSettingsViewModel", "Failed to edit bank account: ${e.message}")
            crashlyticsHelper.recordException(e)
        }
    }

    private suspend fun registerPhoneNumber(phoneNumber: String) {
        try {
            taxiUserRepository.registerPhoneNumber(phoneNumber = phoneNumber)
        } catch (e: Exception) {
            Log.d("TaxiSettingsViewModel", "Failed to register phone number: ${e.message}")
            crashlyticsHelper.recordException(e)
        }
    }

    private suspend fun editBadge(showBadge: Boolean) {
        try {
            taxiUserRepository.editBadge(showBadge = showBadge)
        } catch (e: Exception) {
            Log.d("TaxiSettingsViewModel", "Failed to edit badge: ${e.message}")
            crashlyticsHelper.recordException(e)
        }
    }

    private suspend fun registerResidence(residence: String) {
        try {
            taxiUserRepository.registerResidence(residence = residence)
        } catch (e: Exception) {
            Log.d("TaxiSettingsViewModel", "Failed to register residence: ${e.message}")
            crashlyticsHelper.recordException(e)
        }
    }
}
