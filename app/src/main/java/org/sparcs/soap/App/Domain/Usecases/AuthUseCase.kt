package org.sparcs.soap.App.Domain.Usecases

import android.app.Activity
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Error.Auth.AuthUseCaseError
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Repositories.Ara.AraUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Feed.FeedUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.OTL.OTLUserRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.AuthenticationService
import org.sparcs.soap.App.Domain.Services.AuthenticationServiceProtocol
import org.sparcs.soap.App.Networking.ResponseDTO.Ara.AraSignInResponseDTO
import retrofit2.HttpException
import timber.log.Timber
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
    suspend fun refreshAccessToken(force: Boolean = false)
}

@Singleton
class AuthUseCase @Inject constructor(
    private val authenticationService: AuthenticationServiceProtocol,
    val tokenStorage: TokenStorageProtocol,
    private val araUserRepository: AraUserRepositoryProtocol,
    private val feedUserRepository: FeedUserRepositoryProtocol,
    private val otlUserRepository: OTLUserRepositoryProtocol,
) : AuthUseCaseProtocol {

    private val _isAuthenticated = MutableStateFlow(
        tokenStorage.getRefreshToken() != null    )
    override val isAuthenticatedFlow: Flow<Boolean> = _isAuthenticated.asStateFlow()
    private var refreshJob: Deferred<Unit>? = null
    private var scheduledRefreshJob: Job? = null
    var onTokenRefresh: (() -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val hasAccess = tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()
        val hasRefresh = tokenStorage.getRefreshToken() != null

        _isAuthenticated.value = hasAccess || hasRefresh

        if (hasRefresh) {
            scheduleRefreshToken()
        }
    }

    private fun scheduleRefreshToken() {
        scheduledRefreshJob?.cancel()

        val expirationDate = tokenStorage.getTokenExpirationDate() ?: return
        val bufferMillis = TimeUnit.MINUTES.toMillis(5)
        val delayMillis =
            (expirationDate.time - System.currentTimeMillis() - bufferMillis).coerceAtLeast(0)

        scheduledRefreshJob = coroutineScope.launch {
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            try {
                refreshAccessToken(force = true)
            } catch (e: Exception) {
                Timber.e(e, "Token refresh failed in scheduled job")
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
        return try {
            if (tokenStorage.isTokenExpired()) {
                refreshAccessToken()
            }
            tokenStorage.getAccessToken() ?: throw AuthUseCaseError.NoAccessToken
        } catch (e: Exception) {
            _isAuthenticated.value = false
            throw AuthUseCaseError.NoAccessToken
        }
    }
    override suspend fun refreshAccessToken(force: Boolean) {
        // If a refresh is already in-flight, coalesce by awaiting it
        refreshJob?.let {
            if (it.isActive) {
                it.await()
                return
            }
        }

        val accessToken = tokenStorage.getAccessToken()
        val isExpired = tokenStorage.isTokenExpired()

        if (accessToken != null && !isExpired && !force) {
            Timber.d("[AuthUseCase] Access token is still valid. No refresh needed.")
            scheduleRefreshToken() // reset timer on valid
            return
        }

        val job = coroutineScope.async(Dispatchers.IO) {
            try {
                val currentRefreshToken = tokenStorage.getRefreshToken() ?: run {
                    // No refresh token found, sign out.
                    tokenStorage.clearTokens()
                    _isAuthenticated.value = false
                    cancelRefreshToken()
                    throw AuthUseCaseError.RefreshFailed(Exception("No refresh token available"))
                }

                // Attempts to refresh token using refresh token from Keychain
                val tokenResponse = authenticationService.refreshAccessToken(currentRefreshToken)
                tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)

                _isAuthenticated.value = true
                scheduleRefreshToken() // set timer on success
                onTokenRefresh?.invoke()

                Unit
            } catch (e: Exception) {
                // network error, do not remove tokens on decoding error. only when 401
                val isAuthError = (e as? HttpException)?.code() == 401
                if (isAuthError) {
                    tokenStorage.clearTokens()
                    _isAuthenticated.value = false
                    cancelRefreshToken()
                }
                throw e
            } finally {
                refreshJob = null
            }
        }

        refreshJob = job
        job.await()
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
                Timber.e("Failed to Sign in. agreeTOS failed: ${e.message}")
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