package org.sparcs.soap.app.domain.usecases

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessaging
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.ChannelManager
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.repositories.ara.AraUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.feed.FeedUserRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.otl.OTLUserRepositoryProtocol
import org.sparcs.soap.app.domain.services.AuthenticationService
import org.sparcs.soap.app.domain.services.AuthenticationServiceProtocol
import org.sparcs.soap.app.networking.responseDTO.ara.AraSignInResponseDTO
import org.sparcs.soap.widgets.WidgetSyncHelper
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
    private val fcmUseCase: FCMUseCaseProtocol,
    private val widgetSyncHelper: WidgetSyncHelper
) : AuthUseCaseProtocol {

    private val _isAuthenticated = MutableStateFlow(tokenStorage.getRefreshToken() != null)
    override val isAuthenticatedFlow: Flow<Boolean> = _isAuthenticated.asStateFlow()

    // Coalesce concurrent refresh calls
    private var refreshJob: Deferred<Unit>? = null
    private var scheduledRefreshJob: Job? = null

    // Cooldown: skip refresh attempts for 10s after a failure
    private var lastRefreshFailure: Long = 0
    private var lastRefreshSuccess: Long = 0
    private val refreshCooldownMillis = TimeUnit.SECONDS.toMillis(10)
    private val minRefreshIntervalMillis = TimeUnit.MINUTES.toMillis(5)

    // Called after a successful token refresh
    var onTokenRefresh: (() -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        val hasAccess = tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()
        val hasRefresh = tokenStorage.getRefreshToken() != null

        _isAuthenticated.value = hasAccess || hasRefresh

        scheduleRefreshToken()
        observeForeground()
        if (_isAuthenticated.value) {
            coroutineScope.launch(Dispatchers.IO) {
                syncFcmTokenIfAuthenticated()
            }
        }
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
                            } catch (_: Exception) { /* ignore */ }
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
        val rawDelayMillis = expirationDate.time - System.currentTimeMillis() - bufferMillis
        val delayMillis = rawDelayMillis.coerceAtLeast(minRefreshIntervalMillis)

        scheduledRefreshJob = coroutineScope.launch {
            delay(delayMillis)
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

    private suspend fun syncFcmTokenIfAuthenticated() {
        if (tokenStorage.getRefreshToken() == null) return

        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            fcmUseCase.register(fcmToken)
        } catch (e: Exception) {
            Timber.e(e, "FCM token sync failed")
        }
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
            tokenStorage.getAccessToken() ?: throw AuthUseCaseError.NoAccessToken()
        } catch (_: Exception) {
            throw AuthUseCaseError.NoAccessToken()
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

        val now = System.currentTimeMillis()
        if (now - lastRefreshSuccess < minRefreshIntervalMillis) {
            scheduleRefreshToken()
            return
        }

        if (now - lastRefreshFailure < refreshCooldownMillis) {
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
                lastRefreshSuccess = System.currentTimeMillis()
                scheduleRefreshToken() // set timer on success
                onTokenRefresh?.invoke()
                syncFcmTokenIfAuthenticated()

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

                syncFcmTokenIfAuthenticated()

                widgetSyncHelper.refreshAllWidgets()
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
            ChannelManager.clearIdentity()
            tokenStorage.clearTokens()
            scheduledRefreshJob?.cancel()
            widgetSyncHelper.clearAllWidgets()
            _isAuthenticated.value = false
        }
    }
}