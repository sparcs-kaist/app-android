package org.sparcs.soap.App.Features.Main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Repositories.AppVersionRepository
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiLocationUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.isUpdateRequired
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appVersionRepository: AppVersionRepository,
    private val authUseCase: AuthUseCaseProtocol,
    private val taxiLocationUseCase: TaxiLocationUseCaseProtocol,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _mustUpdate = MutableStateFlow(false)
    val mustUpdate: StateFlow<Boolean> = _mustUpdate.asStateFlow()

    private var lastCheckTime: Long? = null

    val isAuthenticated: StateFlow<Boolean> = authUseCase.isAuthenticatedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            authUseCase.isAuthenticatedFlow.collect {
                _isLoading.value = false
            }
        }
    }

    fun onActivation(currentVersion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            checkUpdateIfNeeded(currentVersion)

            if (!_mustUpdate.value) {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkUpdateIfNeeded(currentVersion: String) {
        val now = System.currentTimeMillis()
        val oneHourInMillis = 3600 * 1000L

        if (lastCheckTime == null || now - lastCheckTime!! > oneHourInMillis) {
            try {
                val versionInfo = appVersionRepository.fetchMinimumVersion()
                versionInfo.android?.let { minVersion ->
                    _mustUpdate.value = currentVersion.isUpdateRequired(minVersion)
                }
                lastCheckTime = now
            } catch (e: Exception) {
                Timber.e(e, "Version check failed")
            }
        }
    }

    fun fetchTaxiLocations() {
        viewModelScope.launch {
            try {
                taxiLocationUseCase.fetchLocations()
            } catch (e: Exception) {
                Timber.e(e, "Location fetch failed")
            }
        }
    }

    fun resetTimer() {
        lastCheckTime = null
    }
}