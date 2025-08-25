package com.example.soap.Domain.Services

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.example.soap.Domain.Enums.AuthenticationServiceError
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Networking.ResponseDTO.Auth.SignInResponseDTO
import com.example.soap.Networking.ResponseDTO.Auth.TokenResponseDTO
import com.example.soap.Networking.RetrofitAPI.AuthApi
import com.example.soap.Shared.Extensions.base64UrlEncodedString
import com.example.soap.Shared.Extensions.sha256
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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


    override suspend fun authenticate(activity: Activity): SignInResponseDTO =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->

            val codeVerifier = generateCodeVerifier()
            val codeChallenge = generateCodeChallenge(codeVerifier)
            val webView = WebView(activity)
            val authURL = Constants.authorizationURL

            val headers = mapOf(
                "Origin" to "sparcsapp",
                "Content-Type" to "application/json"
            )

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let {
                        val uri = Uri.parse(it)
                        val session = uri.getQueryParameter("session")
                        if (!session.isNullOrEmpty()) {

                            val container = activity.findViewById<FrameLayout>(android.R.id.content)
                            container.removeView(webView)

                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                try {
                                    val tokenResponse = exchangeCodeForTokens(session, codeVerifier)
                                    continuation.resume(tokenResponse)
                                } catch (e: Exception) {
                                    Log.e("AuthWebView", "Token exchange failed", e)
                                    continuation.resumeWithException(
                                        AuthenticationServiceError.TokenExchangeFailed(e)
                                    )
                                }
                            }
                            return true
                        }
                    }
                    return false
                }
            }

            val container = activity.findViewById<FrameLayout>(android.R.id.content)
            container.addView(webView)
            webView.loadUrl("$authURL?codeChallenge=$codeChallenge", headers)
            continuation.invokeOnCancellation {
                container.removeView(webView)
            }
        }

    private suspend fun exchangeCodeForTokens(sessionCode: String, codeVerifier: String): SignInResponseDTO {
        val encodedSessionCode = URLEncoder.encode(sessionCode, "UTF-8")
        val encodedVerifier = codeVerifier.toByteArray(StandardCharsets.UTF_8).base64UrlEncodedString()
        val response = authApi.requestTokens(
            cookie = "connect.sid=$encodedSessionCode",
            body = mapOf("codeVerifier" to encodedVerifier)
        )
        tokenStorage.save(response.accessToken, response.refreshToken)
        Log.d("AuthWebView", "TokenResponse received: $response")
        return response
    }


    override suspend fun refreshAccessToken(refreshToken: String): TokenResponseDTO {
        return try {
            val body = mapOf("refreshToken" to refreshToken)
            val response = authApi.refreshTokens(body = body)
            tokenStorage.save(response.accessToken, response.refreshToken)
            response
        } catch (e: Exception) {
            throw AuthenticationServiceError.TokenRefreshFailed(e)
        }
    }

}
