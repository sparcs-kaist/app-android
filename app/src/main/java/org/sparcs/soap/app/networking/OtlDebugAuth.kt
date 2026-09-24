package org.sparcs.soap.app.networking

import okhttp3.Request

internal fun Request.Builder.otlDebugAuth(isDebug: Boolean, token: String): Request.Builder = apply {
    removeHeader("X-SID-AUTH-TOKEN")
    if (isDebug && token.isNotBlank()) header("X-SID-AUTH-TOKEN", token)
}
