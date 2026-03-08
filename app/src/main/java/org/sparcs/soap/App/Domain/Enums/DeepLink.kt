package org.sparcs.soap.App.Domain.Enums

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.sparcs.soap.BuildConfig

object DeepLinkEventBus {
    private val _events = MutableSharedFlow<DeepLink>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    suspend fun post(deepLink: DeepLink) {
        _events.emit(deepLink)
    }
}

sealed class DeepLink {
    data class TaxiInvite(val code: String) : DeepLink()
    data class AraPost(val id: Int) : DeepLink()

    companion object {
        fun fromUri(uri: Uri?): DeepLink? {
            if (uri == null) return null

            val taxiBaseURL = BuildConfig.TAXI_HOST
            val araBaseURL = BuildConfig.ARA_HOST
            return when (uri.host) {
                taxiBaseURL -> {
                    val segments = uri.pathSegments
                    if (segments.size == 2 && segments[0] == "invite") {
                        TaxiInvite(code = segments[1])
                    } else null
                }

                araBaseURL -> {
                    val segments = uri.pathSegments
                    if (segments.size == 2 && segments[0] == "post") {
                        segments[1].toIntOrNull()?.let { AraPost(id = it) }
                    } else null
                }

                else -> null
            }
        }
    }
}