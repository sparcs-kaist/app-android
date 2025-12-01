package com.sparcs.soap.Domain.Usecases

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
import com.sparcs.soap.Domain.Enums.Auth.AuthUseCaseError
import com.sparcs.soap.Domain.Helpers.TokenStorageProtocol
import com.sparcs.soap.Domain.Repositories.Ara.AraUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import com.sparcs.soap.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import com.sparcs.soap.Domain.Services.AuthenticationService
import com.sparcs.soap.Domain.Services.AuthenticationServiceProtocol
import com.sparcs.soap.Networking.ResponseDTO.Ara.AraSignInResponseDTO
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

interface AuthUseCaseProtocol {
    val isAuthenticatedFlow: Flow<Boolean>

    @Throws(Exception::class)
    suspend fun signIn(activity: Activity)

    @Throws(Exception::class)
    suspend fun signOut()

    fun getAccessToken(): String?

    @Throws(Exception::class)
    suspend fun getValidAccessToken(): String

    @Throws(Exception::class)
    suspend fun refreshAccessTokenIfNeeded()
}

@Singleton
class AuthUseCase @Inject constructor(
    private val authenticationService: AuthenticationServiceProtocol,
    val tokenStorage: TokenStorageProtocol,
    private val araUserRepository: AraUserRepositoryProtocol,
    private val feedUserRepository: FeedUserRepositoryProtocol,
    private val otlUserRepository: OTLUserRepositoryProtocol
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
            val tokenResponse = (authenticationService as AuthenticationService).authenticate(activity as ComponentActivity)

            tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)

            // MARK - Sign up Ara
            val userInfo: AraSignInResponseDTO =
                araUserRepository.register(ssoInfo = tokenResponse.ssoInfo)
            try {
                araUserRepository.agreeTOS(userID = userInfo.userID)
            } catch (e: Exception) {
                Log.e("AuthUseCase","Failed to Sign in. agreeTOS failed: ${e.message}")
            }

            // MARK - Sign up Feed
            feedUserRepository.register(ssoInfo = tokenResponse.ssoInfo)

            // MARK - Sign up OTL
            otlUserRepository.register(ssoInfo = tokenResponse.ssoInfo)

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