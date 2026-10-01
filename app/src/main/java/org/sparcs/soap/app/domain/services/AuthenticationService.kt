package org.sparcs.soap.app.domain.services

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.sparcs.soap.app.domain.error.auth.AuthenticationServiceError
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.repositories.AuthRepositoryProtocol
import org.sparcs.soap.app.networking.responseDTO.auth.SignInResponseDTO
import org.sparcs.soap.app.networking.responseDTO.auth.TokenResponseDTO
import org.sparcs.soap.app.shared.extensions.base64UrlEncodedString
import org.sparcs.soap.app.shared.extensions.sha256
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
    private val authRepository: AuthRepositoryProtocol,
) : AuthenticationServiceProtocol {

    private fun generateState(): String {
        val sr = SecureRandom()
        val bytes = ByteArray(16)
        sr.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun generateVerifierBytes(): ByteArray {
        val sr = SecureRandom()
        val bytes = ByteArray(32)
        sr.nextBytes(bytes)
        return bytes
    }

    override suspend fun authenticate(activity: ComponentActivity): SignInResponseDTO =
        suspendCancellableCoroutine { continuation ->

            val state = generateState()
            val verifierBytes = generateVerifierBytes()
            val codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes)
            val challenge = verifierBytes.sha256().base64UrlEncodedString()

            val authURL = "${Constants.authorizationURL}&state=$state&challenge=$challenge"

            var isAuthProcessing = false
            var isBrowserLaunched = false

            val observer = object : DefaultLifecycleObserver {
                override fun onPause(owner: LifecycleOwner) {
                    isBrowserLaunched = true
                }

                override fun onResume(owner: LifecycleOwner) {
                    if (isBrowserLaunched && continuation.isActive && !isAuthProcessing) {
                        continuation.cancel(AuthenticationServiceError.UserCancelled())
                        AuthenticationCallbackHandler.clearCallback()
                    }
                }
            }

            try {
                activity.lifecycle.addObserver(observer)

                val intent = Intent(Intent.ACTION_VIEW, authURL.toUri())
                activity.startActivity(intent)

                AuthenticationCallbackHandler.setCallback { uri ->
                    if (!continuation.isActive) return@setCallback
                    isAuthProcessing = true

                    val session = uri.getQueryParameter("session")
                    val returnedState = uri.getQueryParameter("state")

                    if (!session.isNullOrEmpty() && returnedState == state) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val tokenResponse =
                                    authRepository.requestToken(session, codeVerifier)
                                continuation.resume(tokenResponse)
                            } catch (e: Exception) {
                                Timber.e(e, "Token exchange failed")
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
                        Timber.e("Invalid callback URI or state mismatch. session=$session, returnedState=$returnedState, expectedState=$state")
                        continuation.resumeWithException(AuthenticationServiceError.InvalidCallbackURL())
                        AuthenticationCallbackHandler.clearCallback()
                    }
                }

                continuation.invokeOnCancellation {
                    AuthenticationCallbackHandler.clearCallback()
                    activity.lifecycle.removeObserver(observer)
                }

            } catch (_: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(AuthenticationServiceError.Unknown())
                }
            }
        }


    override suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO {
        return try {
            authRepository.refreshToken(refreshToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is UnknownHostException || e is SocketTimeoutException) {
                Timber.w("Network error during token refresh. Stopping retry.")
            } else {
                Timber.e(e, "Failed to refresh access token")
            }
            throw AuthenticationServiceError.TokenRefreshFailed(e)
        }
    }
}
