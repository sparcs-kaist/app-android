package com.sparcs.soap.Features.Main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparcs.soap.Domain.Usecases.AuthUseCaseProtocol
import com.sparcs.soap.Domain.Usecases.UserUseCaseProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authUseCase: AuthUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    val isAuthenticated: StateFlow<Boolean?> = authUseCase.isAuthenticatedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            authUseCase.isAuthenticatedFlow.collect { authed ->
                _isLoading.value = false
                if (authed) {
                    refreshAccessTokenIfNeeded()
                }
            }
        }
    }

    private fun refreshAccessTokenIfNeeded() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authUseCase.refreshAccessTokenIfNeeded()
                userUseCase.fetchUsers()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Token refresh failed", e)
            }
            _isLoading.value = false
        }
    }
}