package org.sparcs.soap.App.Features.Main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Repositories.AppVersionRepository
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.isUpdateRequired
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val authUseCase: AuthUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mustUpdate = MutableStateFlow(false)
    val mustUpdate: StateFlow<Boolean> = _mustUpdate

    val isAuthenticated: StateFlow<Boolean?> = authUseCase.isAuthenticatedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            authUseCase.isAuthenticatedFlow.collect { authed ->
                if (authed) {
                    launch {
                        refreshAccessTokenIfNeeded()
                    }
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun refreshAccessTokenIfNeeded() {
        try {
            authUseCase.refreshAccessTokenIfNeeded()
            userUseCase.fetchUsers()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Data fetch failed", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun checkAuthOnResume() {
        viewModelScope.launch {
            authUseCase.refreshAccessTokenIfNeeded(force = false)
        }
    }

    fun checkVersion(currentVersion: String) {
        viewModelScope.launch {
            val versionInfo = appVersionRepository.fetchMinimumVersion()
            versionInfo.android?.let { minVersion ->
                _mustUpdate.value = currentVersion.isUpdateRequired(minVersion)
            }
        }
    }
}