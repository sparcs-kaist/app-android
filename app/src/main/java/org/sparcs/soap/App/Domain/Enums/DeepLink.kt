package org.sparcs.soap.App.Domain.Enums

import android.net.Uri

sealed class DeepLink {
    data class TaxiInvite(val code: String) : DeepLink()
    data class AraPost(val id: Int) : DeepLink()

    companion object {
        fun fromUri(uri: Uri?): DeepLink? {
            if (uri == null) return null

            return when (uri.host) {
                "taxi.sparcs.org" -> {
                    val segments = uri.pathSegments
                    if (segments.size == 2 && segments[0] == "invite") {
                        TaxiInvite(code = segments[1])
                    } else null
                }

                "newara.sparcs.org" -> {
                    val segments = uri.pathSegments
                    if (segments.size == 2 && segments[0] == "post") {
                        val id = segments[1].toIntOrNull()
                        if (id != null) AraPost(id = id) else null
                    } else null
                }

                else -> null
            }
        }
    }
}