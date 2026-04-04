package org.sparcs.soap.App.Domain.Usecases

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _isAuthenticated = MutableStateFlow(tokenStorage.getRefreshToken() != null)
    override val isAuthenticatedFlow: Flow<Boolean> = _isAuthenticated.asStateFlow()

    // Coalesce concurrent refresh calls
    private var refreshJob: Deferred<Unit>? = null
    private var scheduledRefreshJob: Job? = null

    // Cooldown: skip refresh attempts for 10s after a failure
    private var lastRefreshFailure: Long = 0
    private val refreshCooldownMillis = 10_000L

    // Called after a successful token refresh
    var onTokenRefresh: (() -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val hasAccess = tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()
        val hasRefresh = tokenStorage.getRefreshToken() != null

        _isAuthenticated.value = hasAccess || hasRefresh

        scheduleRefreshToken()
        observeForeground()
    }

    // MARK: - Foreground Refresh
    private fun observeForeground() {
        coroutineScope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    coroutineScope.launch {
                        if (_isAuthenticated.value) {
                            try {
                                refreshAccessToken(force = false)
                            } catch (e: Exception) { /* ignore */
                            }
                        }
                    }
                }
            })
        }
    }

    private fun scheduleRefreshToken() {
        scheduledRefreshJob?.cancel()

        val expirationDate = tokenStorage.getTokenExpirationDate() ?: return
        val bufferMillis = TimeUnit.MINUTES.toMillis(5)
        val delayMillis =
            (expirationDate.time - System.currentTimeMillis() - bufferMillis).coerceAtLeast(0)

        scheduledRefreshJob = coroutineScope.launch {
            if (delayMillis > 0) delay(delayMillis)
            try {
                refreshAccessToken(force = true)
            } catch (e: Exception) {
                Timber.e(e, "Scheduled token refresh failed")
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
        if (!force) {
            refreshJob?.let {
                if (it.isActive) {
                    it.await()
                    return
                }
            }
        } else {
            refreshJob?.cancel()
        }

        if (System.currentTimeMillis() - lastRefreshFailure < refreshCooldownMillis) {
            throw AuthUseCaseError.RefreshFailed(Exception("Refresh on cooldown"))
        }

        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null && !tokenStorage.isTokenExpired() && !force) {
            Timber.d("[AuthUseCase] Still valid. No refresh needed.")
            scheduleRefreshToken()
            return
        }

        val job = coroutineScope.async(Dispatchers.IO) {
            try {
                val currentRefreshToken = tokenStorage.getRefreshToken() ?: run {
                    // No refresh token found, sign out.
                    signOut()
                    throw AuthUseCaseError.RefreshFailed(Exception("No refresh token available"))
                }

                // Attempts to refresh token using refresh token from Keychain
                val tokenResponse = authenticationService.refreshAccessToken(currentRefreshToken)
                tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)

                _isAuthenticated.value = true
                lastRefreshFailure = 0
                scheduleRefreshToken() // set timer on success
                onTokenRefresh?.invoke()

                Unit
            } catch (e: Exception) {
                lastRefreshFailure = System.currentTimeMillis()
                if ((e as? HttpException)?.code() == 401) {
                    signOut()
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
            val tokenResponse =
                (authenticationService as AuthenticationService).authenticate(activity as ComponentActivity)
            withContext(Dispatchers.IO + NonCancellable) {
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
            }
        } catch (e: Exception) {
            tokenStorage.clearTokens()
            _isAuthenticated.value = false
            cancelRefreshToken()
            throw AuthUseCaseError.SignInFailed(e)
        }
    }

    override suspend fun signOut() {
        withContext(Dispatchers.IO + NonCancellable) {
            tokenStorage.clearTokens()
            scheduledRefreshJob?.cancel()
            _isAuthenticated.value = false
        }
    }
}