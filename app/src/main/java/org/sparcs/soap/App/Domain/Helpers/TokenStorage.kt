package org.sparcs.soap.App.Domain.Helpers

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
)  : TokenStorageProtocol {

    companion object {
        private const val PREF_FILE_NAME = "secure_token_storage"
        private const val ACCESS_TOKEN_KEY = "accessToken"
        private const val REFRESH_TOKEN_KEY = "refreshToken"
        private const val TOKEN_EXPIRATION_KEY = "tokenExpiration"

        // refresh 5 min before it expires
        private const val EXPIRATION_BUFFER_MS = 5 * 60 * 1000L

        //로그 출력용
        private const val TAG = "TokenStorage"

        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_KEY_ALIAS = "TokenStorageAESKey"
        private const val AES_MODE = "AES/GCM/NoPadding"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    private val charset = Charset.forName("UTF-8")

    init {
        if (!keyStore.containsAlias(AES_KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(AES_KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(charset))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(cipherText: String): String? {
        return try {
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            val iv = combined.sliceArray(0 until 12)
            val encrypted = combined.sliceArray(12 until combined.size)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), charset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt token", e)
            null
        }
    }

    override fun save(accessToken: String, refreshToken: String) {
        val expiration = extractExpirationDate(accessToken)?.time ?: 0L
        val a = encrypt(accessToken)
        val r = encrypt(refreshToken)
        prefs.edit().apply {
            putString(ACCESS_TOKEN_KEY, a)
            putString(REFRESH_TOKEN_KEY, r)
            putLong(TOKEN_EXPIRATION_KEY, expiration)
            apply()
        }
    }

    override fun getAccessToken(): String? {
        val encrypted = prefs.getString(ACCESS_TOKEN_KEY, null) ?: return null
        return decrypt(encrypted)
    }

    override fun getRefreshToken(): String? {
        val encrypted = prefs.getString(REFRESH_TOKEN_KEY, null) ?: return null
        return decrypt(encrypted)
    }

    override fun isTokenExpired(): Boolean {
        val expiration = prefs.getLong(TOKEN_EXPIRATION_KEY, 0L)
        if (expiration == 0L) return true

        val currentTime = Date().time
        return currentTime + EXPIRATION_BUFFER_MS >= expiration
    }

    override fun getTokenExpirationDate(): Date? {
        val expiration = prefs.getLong(TOKEN_EXPIRATION_KEY, 0L)
        return if (expiration != 0L) Date(expiration) else null
    }

    override fun clearTokens() {
        prefs.edit().apply {
            remove(ACCESS_TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove(TOKEN_EXPIRATION_KEY)
            apply()
        }
    }

    private fun extractExpirationDate(jwtToken: String): Date? {
        val parts = jwtToken.split(".")
        if (parts.size != 3) return null
        val payload = parts[1]
        return try {
            val decodedBytes =
                Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val json = String(decodedBytes)
            val map: Map<String, Any> = Gson().fromJson(json, Map::class.java) as Map<String, Any>
            val exp = (map["exp"] as? Double)?.toLong() ?: return null
            Date(exp * 1000)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract expiration from token", e)
            null
        }
    }
}

