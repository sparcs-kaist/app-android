package org.sparcs.soap.App.Features.Settings.Taxi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiUserRepository
import org.sparcs.soap.App.Domain.Services.CrashlyticsService
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.isNetworkError
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

interface TaxiSettingsViewModelProtocol {
    var bankName: String?
    var bankNumber: String
    var phoneNumber: String
    var showBadge: Boolean
    var residence: String
    var showAlert: Boolean
    var alertMessageRes: Int?

    val user: TaxiUser?
    val state: StateFlow<TaxiSettingsViewModel.ViewState>

    suspend fun fetchUser()
    suspend fun editInformation()
}

@HiltViewModel
class TaxiSettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val taxiUserRepository: TaxiUserRepository,
    private val crashlyticsService: CrashlyticsService,
) : ViewModel(), TaxiSettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(
            val error: Exception,
            val resId: Int? = null,
        ) : ViewState()
    }

    enum class ErrorType {
        FETCH, BANK, BADGE, PHONE, RESIDENCE
    }

    override var bankName by mutableStateOf<String?>(null)
    override var bankNumber by mutableStateOf("")

    override var phoneNumber by mutableStateOf("")
    override var showBadge by mutableStateOf(false)
    override var residence by mutableStateOf("")

    override var showAlert by mutableStateOf(false)
    override var alertMessageRes by mutableStateOf<Int?>(null)

    override var user by mutableStateOf<TaxiUser?>(null)

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state

    override suspend fun fetchUser() {
        _state.value = ViewState.Loading
        val fetchedUser = userUseCase.taxiUser
        if (fetchedUser == null) {
            _state.value = ViewState.Error(NoSuchElementException(), R.string.error_taxi_user_not_found)
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
            Timber.e("Failed to fetch user: ${e.message}")
            handleException(e, ErrorType.FETCH)
        }
    }

    private suspend fun editBankAccount(bankName: String, bankNumber: String) {
        try {
            taxiUserRepository.editBankAccount(account = "$bankName $bankNumber")
        } catch (e: Exception) {
            Timber.e("Failed to edit bank account: ${e.message}")
            handleException(e, ErrorType.BANK)
        }
    }

    private suspend fun registerPhoneNumber(phoneNumber: String) {
        try {
            taxiUserRepository.registerPhoneNumber(phoneNumber = phoneNumber)
        } catch (e: Exception) {
            Timber.e("Failed to register phone number: ${e.message}")
            handleException(e, ErrorType.PHONE)
        }
    }

    private suspend fun editBadge(showBadge: Boolean) {
        try {
            taxiUserRepository.editBadge(showBadge = showBadge)
        } catch (e: Exception) {
            Timber.e("Failed to edit badge: ${e.message}")
            handleException(e, ErrorType.BADGE)
        }
    }

    private suspend fun registerResidence(residence: String) {
        try {
            taxiUserRepository.registerResidence(residence = residence)
        } catch (e: Exception) {
            Timber.tag("TaxiSettingsViewModel").e("Failed to register residence: ${e.message}")
            handleException(e, ErrorType.RESIDENCE)
        }
    }

    private fun handleException(error: Exception, type: ErrorType) {
        val messageRes = if (error.isNetworkError()) {
            R.string.network_connection_error
        } else {
            crashlyticsService.recordException(error)
            when (type) {
                ErrorType.BADGE -> R.string.badge_information_error
                ErrorType.BANK -> R.string.bank_account_error
                ErrorType.PHONE -> R.string.phone_verification_error
                ErrorType.FETCH -> R.string.fetch_user_error
                ErrorType.RESIDENCE -> R.string.residence_information_error
            }
        }

        if (type == ErrorType.FETCH) {
            _state.value = ViewState.Error(error, messageRes)
        } else {
            alertMessageRes = messageRes
            showAlert = true
        }
    }
}
