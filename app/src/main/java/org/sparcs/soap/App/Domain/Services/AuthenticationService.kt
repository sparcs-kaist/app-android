package org.sparcs.soap.App.Domain.Services

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.sparcs.soap.App.Domain.Error.Auth.AuthenticationServiceError
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Repositories.AuthRepositoryProtocol
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.SignInResponseDTO
import org.sparcs.soap.App.Networking.ResponseDTO.Auth.TokenResponseDTO
import org.sparcs.soap.App.Shared.Extensions.base64UrlEncodedString
import org.sparcs.soap.App.Shared.Extensions.sha256
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface AuthenticationServiceProtocol {

    @Throws(Exception::class)
    suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO

    @Throws(Exception::class)
    suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO
}

object AuthenticationCallbackHandler {
    private var callback: ((Uri) -> Unit)? = null

    fun setCallback(cb: (Uri) -> Unit) {
        callback = cb
    }

    fun handleUri(uri: Uri) {
        callback?.invoke(uri)
        callback = null
    }

    fun clearCallback() {
        callback = null
    }
}

class AuthenticationService @Inject constructor(
    private val authRepository: AuthRepositoryProtocol
) : AuthenticationServiceProtocol {

    private fun generateCodeVerifier(): String {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code)
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        return codeVerifier.toByteArray(StandardCharsets.UTF_8).sha256().base64UrlEncodedString()
    }

    override suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO =
        suspendCancellableCoroutine { continuation ->

            val codeVerifier = generateCodeVerifier()
            val codeChallenge = generateCodeChallenge(codeVerifier)
            val authURL = Constants.authorizationURL + codeChallenge

            var isAuthProcessing = false
            var isBrowserLaunched = false

            val observer = object : DefaultLifecycleObserver {
                override fun onPause(owner: LifecycleOwner) {
                    isBrowserLaunched = true
                }

                override fun onResume(owner: LifecycleOwner) {
                    if (isBrowserLaunched && continuation.isActive && !isAuthProcessing) {
                        continuation.cancel(AuthenticationServiceError.UserCancelled)
                        AuthenticationCallbackHandler.clearCallback()
                    }
                }
            }

            try {
                activity.lifecycle.addObserver(observer)

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authURL))
                activity.startActivity(intent)

                AuthenticationCallbackHandler.setCallback { uri ->
                    if (!continuation.isActive) return@setCallback
                    isAuthProcessing = true

                    val session = uri.getQueryParameter("session")
                    if (!session.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val tokenResponse = authRepository.requestToken(session, codeVerifier)
                                continuation.resume(tokenResponse)
                            } catch (e: Exception) {
                                Log.e("AuthWebView", "Token exchange failed", e)
                                continuation.resumeWithException(
                                    AuthenticationServiceError.TokenExchangeFailed(
                                        e
                                    )
                                )
                            } finally {
                                AuthenticationCallbackHandler.clearCallback()
                            }
                        }
                    } else {
                        continuation.resumeWithException(AuthenticationServiceError.InvalidCallbackURL)
                        AuthenticationCallbackHandler.clearCallback()
                    }
                }

                continuation.invokeOnCancellation {
                    AuthenticationCallbackHandler.clearCallback()
                    activity.lifecycle.removeObserver(observer)
                }

            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(AuthenticationServiceError.Unknown)
                }
            }
        }


    override suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO {
        return try {
            authRepository.refreshToken(refreshToken)
        } catch (e: Exception) {
            if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                Log.w("AuthService", "Network error during token refresh. Stopping retry.")
            } else {
                Log.e("AuthService", "Failed to refresh access token", e)
            }
            throw AuthenticationServiceError.TokenRefreshFailed(e)
        }
    }
}