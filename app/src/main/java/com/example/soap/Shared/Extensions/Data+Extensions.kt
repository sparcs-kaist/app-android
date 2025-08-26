package com.example.soap.Shared.Extensions

import android.util.Base64
import java.security.MessageDigest

fun ByteArray.sha256(): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(this)
}

fun ByteArray.base64UrlEncodedString(): String {
    return Base64.encodeToString(this, Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE)
}
