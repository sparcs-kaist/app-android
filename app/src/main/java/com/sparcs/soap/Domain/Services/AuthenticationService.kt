package com.sparcs.soap.Domain.Services

import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sparcs.soap.Domain.Enums.Auth.AuthenticationServiceError
import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Helpers.TokenStorageProtocol
import com.sparcs.soap.Networking.ResponseDTO.Auth.SignInResponseDTO
import com.sparcs.soap.Networking.ResponseDTO.Auth.TokenResponseDTO
import com.sparcs.soap.Networking.RetrofitAPI.AuthApi
import com.sparcs.soap.Shared.Extensions.base64UrlEncodedString
import com.sparcs.soap.Shared.Extensions.sha256
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


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
    @Named("Auth") private val authApi: AuthApi,
    private val tokenStorage: TokenStorageProtocol
): AuthenticationServiceProtocol {

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

            val observer = object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    if (continuation.isActive) {
                        continuation.cancel(AuthenticationServiceError.UserCancelled)
                        AuthenticationCallbackHandler.clearCallback()
                    }
                }
            }

            try {
                activity.lifecycle.addObserver(observer)

                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.launchUrl(activity, Uri.parse(authURL))

                AuthenticationCallbackHandler.setCallback { uri ->
                    if (!continuation.isActive) return@setCallback

                    val session = uri.getQueryParameter("session")
                    if (!session.isNullOrEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val tokenResponse = exchangeCodeForTokens(session, codeVerifier)
                                continuation.resume(tokenResponse)
                            } catch (e: Exception) {
                                Log.e("AuthWebView", "Token exchange failed", e)
                                continuation.resumeWithException(AuthenticationServiceError.TokenExchangeFailed(e))
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


    private suspend fun exchangeCodeForTokens(sessionCode: String, codeVerifier: String): SignInResponseDTO {
        val encodedSessionCode = withContext(Dispatchers.IO) {
            URLEncoder.encode(sessionCode, "UTF-8")
        }
        val encodedVerifier = codeVerifier.toByteArray(StandardCharsets.UTF_8).base64UrlEncodedString()
        val response = authApi.requestTokens(
            cookie = "connect.sid=$encodedSessionCode",
            body = mapOf("codeVerifier" to encodedVerifier)
        )
        tokenStorage.save(response.accessToken, response.refreshToken)
        return response
    }


    override suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO {
        return try {
            val body = mapOf("refreshToken" to refreshToken)
            val response = authApi.refreshTokens(body = body)
            tokenStorage.save(response.accessToken, response.refreshToken)
            response
        } catch (e: Exception) {
            Log.e("AuthService", "Failed to refresh access token", e)
            throw AuthenticationServiceError.TokenRefreshFailed(e)
        }
    }
}
