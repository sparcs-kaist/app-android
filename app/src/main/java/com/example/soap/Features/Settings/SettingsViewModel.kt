package com.example.soap.Features.Settings

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Taxi.TaxiUserRepositoryProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userUseCase: UserUseCaseProtocol,
    private val taxiUserRepository: TaxiUserRepositoryProtocol
) : ViewModel(), SettingsViewModelProtocol {

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
    }

    // MARK: - Mock data
    // TODO: implement API call & data structures
    override var araAllowNSFWPosts = mutableStateOf(false)
    override var araAllowPoliticalPosts = mutableStateOf(false)
    override var araBlockedUsers = mutableStateOf(listOf("유능한 시조새_0b4c"))
    override var taxiBankName = mutableStateOf<String?>(null)
    override var taxiBankNumber = mutableStateOf("")
    override var otlMajor = mutableStateOf("School of Computer Science")
    override val otlMajorList = listOf(
        "School of Computer Science",
        "School of Electrical Engineering",
        "School of Business"
    )

    // MARK: - Properties
    override val taxiUser = MutableStateFlow<TaxiUser?>(null)

    private val _taxiState = MutableStateFlow<ViewState>(ViewState.Loading)
    override val taxiState: StateFlow<ViewState> = _taxiState.asStateFlow()

    // MARK: - Functions
    override suspend fun fetchTaxiUser() {
        userUseCase.fetchUsers()
        val user = userUseCase.taxiUser
        taxiUser.value = user
        taxiBankName.value = user?.account?.split(" ")?.firstOrNull()
        taxiBankNumber.value = user?.account?.split(" ")?.lastOrNull() ?: ""
        _taxiState.value = ViewState.Loaded
    }

    override suspend fun taxiEditBankAccount(account: String) {
        try {
            taxiUserRepository.editBankAccount(account)
            taxiBankNumber.value = account
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to edit bank account: ${e.localizedMessage}")
        }
    }
}
