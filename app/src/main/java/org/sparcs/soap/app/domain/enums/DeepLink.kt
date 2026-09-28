package org.sparcs.soap.app.domain.enums

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.sparcs.soap.BuildConfig
import org.sparcs.soap.app.domain.helpers.Constants

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
    data class FeedPost(val postID: String, val commentID: String? = null) : DeepLink() {
        fun toUri(): Uri = Uri.parse(Constants.FEED_SHARE_URL).buildUpon()
            .appendPath(postID)
            .apply { commentID?.let { appendQueryParameter("commentId", it) } }
            .build()
    }
    data object Timetable : DeepLink()

    companion object {
        fun fromUri(uri: Uri?): DeepLink? {
            if (uri == null) return null

            val feedBase = Uri.parse(Constants.FEED_SHARE_URL)
            if (uri.scheme == feedBase.scheme && uri.host == feedBase.host &&
                uri.pathSegments.size == 2 && uri.pathSegments.first() == "feed") {
                val postID = uri.pathSegments[1].takeIf { it.isNotBlank() } ?: return null
                val commentID = (uri.getQueryParameter("commentId") ?: uri.getQueryParameter("comment_id"))
                    ?.takeIf { it.isNotBlank() }
                return FeedPost(postID, commentID)
            }

            if (uri.scheme == "sparcsapp") {
                return when (uri.host) {
                    "otl" -> if (uri.pathSegments.contains("timetable")) Timetable else null
                    else -> null
                }
            }

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
