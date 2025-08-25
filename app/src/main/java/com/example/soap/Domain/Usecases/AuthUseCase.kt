package com.example.soap.Domain.Usecases

import android.app.Activity
import android.util.Log
import com.example.soap.Domain.Enums.AuthUseCaseError
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Services.AuthenticationService
import com.example.soap.Domain.Services.AuthenticationServiceProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthUseCase @Inject constructor(
    val authenticationService: AuthenticationServiceProtocol,
    val tokenStorage: TokenStorageProtocol,
//    private val araUserRepository: AraUserRepositoryProtocol
) : AuthUseCaseProtocol {

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticatedFlow: Flow<Boolean> = _isAuthenticated.asStateFlow()

    private var refreshJob: Job? = null
    private var isRefreshing = false
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        _isAuthenticated.value =
            tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()
        scheduleRefreshToken()
    }

    private fun scheduleRefreshToken() {
        refreshJob?.cancel()
        val expirationDate = tokenStorage.getTokenExpirationDate() ?: return
        val bufferMillis = TimeUnit.MINUTES.toMillis(5)
        val delayMillis = (expirationDate.time - System.currentTimeMillis() - bufferMillis).coerceAtLeast(0)
        if (delayMillis > 0) {
            refreshJob = coroutineScope.launch {
                delay(delayMillis)
                try {
                    refreshAccessTokenIfNeeded()
                } catch (_: Exception) { }
            }
        }
    }

    private fun cancelRefreshToken() {
        refreshJob?.cancel()
        refreshJob = null
    }

    override fun getAccessToken(): String? {
        if (tokenStorage.isTokenExpired()) return null
        return tokenStorage.getAccessToken()
    }

    override suspend fun getValidAccessToken(): String {
        if (tokenStorage.isTokenExpired()) {
            refreshAccessTokenIfNeeded()
        }
        return tokenStorage.getAccessToken() ?: throw AuthUseCaseError.NoAccessToken
    }

    override suspend fun refreshAccessTokenIfNeeded() {

        if (isRefreshing) return
        isRefreshing = true

        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken == null) {
            Log.d("AuthUseCase", "No refresh token available, marking as unauthenticated")
            _isAuthenticated.value = false
            cancelRefreshToken()
            isRefreshing = false
            return
        }

        try {
            val tokenResponse = authenticationService.refreshAccessToken(refreshToken)
            tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)
            _isAuthenticated.value = true
            scheduleRefreshToken()
        } catch (e: Exception) {
            Log.d("AuthUseCase", "Failed to refresh token, marking as unauthenticated", e)
            tokenStorage.clearTokens()
            _isAuthenticated.value = false
            cancelRefreshToken()
        } finally {
            isRefreshing = false
        }
    }

    override suspend fun signIn(activity: Activity) {
        try {
            val tokenResponse = (authenticationService as AuthenticationService).authenticate(activity)

            tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)
            _isAuthenticated.value = true
            scheduleRefreshToken()

        } catch (e: Exception) {
            tokenStorage.clearTokens()
            _isAuthenticated.value = false
            cancelRefreshToken()
            throw AuthUseCaseError.SignInFailed(e)
        }
    }

    override suspend fun signOut() {
        tokenStorage.clearTokens()
        _isAuthenticated.value = false
        cancelRefreshToken()
    }
}