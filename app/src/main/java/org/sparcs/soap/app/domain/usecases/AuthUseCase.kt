package org.sparcs.soap.app.domain.usecases

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.ChannelManager
import org.sparcs.soap.app.cache.TimetableCache
import org.sparcs.soap.app.domain.error.auth.AuthUseCaseError
import org.sparcs.soap.app.domain.error.auth.AuthenticationServiceError
import org.sparcs.soap.app.domain.helpers.TokenRefreshCoordinator
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
    private val widgetSyncHelper: WidgetSyncHelper,
    private val timetableCache: TimetableCache,
) : AuthUseCaseProtocol {

    private val _isAuthenticated = MutableStateFlow(tokenStorage.hasStoredRefreshToken())
    override val isAuthenticatedFlow: Flow<Boolean> = _isAuthenticated.asStateFlow()

    private val sessionLock = Any()
    private var sessionRevision = 0L

    private var scheduledRefreshJob: Job? = null

    // Cooldown: skip refresh attempts for 10s after a failure
    private var lastRefreshFailure: Long = 0
    private var lastRefreshError: Exception? = null
    private val refreshCooldownMillis = TimeUnit.SECONDS.toMillis(10)
    private val minRefreshIntervalMillis = TimeUnit.MINUTES.toMillis(5)

    // Called after a successful token refresh
    var onTokenRefresh: (() -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val refreshCoordinator = TokenRefreshCoordinator(coroutineScope)

    init {
        val hasAccess = tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()
        val hasRefresh = tokenStorage.hasStoredRefreshToken()

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
                        try {
                            refreshAccessToken(force = false)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) { /* ignore */ }
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
                refreshAccessToken(force = false)
            } catch (e: Exception) {
                Timber.e(e, "Scheduled token refresh failed")
            }
        }
    }

    private fun cancelRefreshToken() {
        refreshCoordinator.cancel()
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
        if (tokenStorage.getAccessToken() == null || tokenStorage.isTokenExpired()) {
            refreshAccessToken()
        }
        return tokenStorage.getAccessToken() ?: throw AuthUseCaseError.NoAccessToken()
    }

    override suspend fun refreshAccessToken(force: Boolean) {
        refreshCoordinator.refresh {
            performTokenRefresh(force)
        }
    }

    private suspend fun performTokenRefresh(force: Boolean) {
        val revision = synchronized(sessionLock) { sessionRevision }
        val now = System.currentTimeMillis()
        synchronized(sessionLock) {
            if (sessionRevision != revision) throw CancellationException("Authentication session changed")
            if (!force && tokenStorage.getAccessToken() != null && !tokenStorage.isTokenExpired()) {
                _isAuthenticated.value = true
                scheduleRefreshToken()
                return
            }
        }
        if (now - lastRefreshFailure < refreshCooldownMillis) {
            throw AuthUseCaseError.RefreshFailed(lastRefreshError ?: Exception("Refresh on cooldown"))
        }

        val currentRefreshToken = tokenStorage.readRefreshToken() ?: run {
            clearSession(revision)
            throw AuthUseCaseError.NoAccessToken()
        }

        val tokenResponse = try {
            authenticationService.refreshAccessToken(currentRefreshToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastRefreshFailure = System.currentTimeMillis()
            lastRefreshError = e
            if (isRefreshTokenRejected(httpStatusCode(e))) {
                clearSession(revision)
            }
            throw e
        }

        synchronized(sessionLock) {
            if (sessionRevision != revision) throw CancellationException("Authentication session changed")
            tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)
            _isAuthenticated.value = true
        }
        lastRefreshFailure = 0
        lastRefreshError = null
        scheduleRefreshToken()

        coroutineScope.launch {
            try {
                onTokenRefresh?.invoke()
                syncFcmTokenIfAuthenticated()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Post-refresh side effect failed")
            }
        }
    }

    override suspend fun signIn(activity: Activity) {
        val revision = synchronized(sessionLock) { ++sessionRevision }
        try {
            val tokenResponse =
                (authenticationService as AuthenticationService).authenticate(activity as ComponentActivity)
            withContext(Dispatchers.IO + NonCancellable) {
                synchronized(sessionLock) {
                    if (sessionRevision != revision) throw CancellationException("Authentication session changed")
                    tokenStorage.save(tokenResponse.accessToken, tokenResponse.refreshToken)
                }

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

                synchronized(sessionLock) {
                    if (sessionRevision != revision) throw CancellationException("Authentication session changed")
                    _isAuthenticated.value = true
                    scheduleRefreshToken()
                }
                widgetSyncHelper.refreshAllWidgets()
            }
        } catch (e: Exception) {
            clearSession(revision)
            cancelRefreshToken()
            if (e is CancellationException) throw e
            throw AuthUseCaseError.SignInFailed(e)
        }
    }

    private fun isRefreshTokenRejected(code: Int?): Boolean {
        if (code == null) return false
        return code == 400 || code == 401
    }

    private fun httpStatusCode(throwable: Throwable?): Int? {
        var current: Throwable? = throwable
        val seen = mutableSetOf<Throwable>()
        while (current != null && seen.add(current)) {
            current = when (current) {
                is HttpException -> return current.code()
                is AuthenticationServiceError.TokenRefreshFailed -> current.error
                is AuthenticationServiceError.TokenExchangeFailed -> current.error
                else -> current.cause
            }
        }
        return null
    }

    override suspend fun signOut() = clearSession()

    private suspend fun clearSession(expectedRevision: Long? = null) {
        withContext(Dispatchers.IO + NonCancellable) {
            synchronized(sessionLock) {
                if (expectedRevision != null && sessionRevision != expectedRevision) return@withContext
                sessionRevision++
                tokenStorage.clearTokens()
                _isAuthenticated.value = false
            }
            ChannelManager.clearIdentity()
            timetableCache.clear()
            scheduledRefreshJob?.cancel()
            widgetSyncHelper.clearAllWidgets()
        }
    }
}
