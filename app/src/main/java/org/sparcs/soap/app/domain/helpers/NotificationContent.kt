package org.sparcs.soap.app.domain.helpers

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.sparcs.soap.R
import java.util.Locale

internal data class NotificationContent(val title: String, val body: String) {
    companion object {
        fun fromMessage(context: Context, message: RemoteMessage): NotificationContent? {
            val data = message.data
            val notification = message.notification
            val titleKey = data["title_loc_key"] ?: notification?.titleLocalizationKey
            val bodyKey = data["body_loc_key"] ?: notification?.bodyLocalizationKey
            val title = localize(
                context, titleKey,
                arguments(data["title_loc_args"]) ?: notification?.titleLocalizationArgs,
                notification?.title ?: data["title"],
            )
            val body = localize(
                context, bodyKey,
                arguments(data["body_loc_args"]) ?: notification?.bodyLocalizationArgs,
                notification?.body ?: data["body"],
            )
            if (title.isNullOrBlank() && body.isNullOrBlank()) return null
            return NotificationContent(title?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.app_name), body.orEmpty())
        }

        private fun localize(
            context: Context,
            key: String?,
            args: Array<String>?,
            fallback: String?,
        ): String? {
            val resourceID = when (key?.uppercase(Locale.ROOT)) {
                "NEW_COMMENT" -> R.string.new_comment
                "NEW_REPLY" -> R.string.new_reply
                else -> key?.let {
                    context.resources.getIdentifier(
                        it.lowercase(Locale.ROOT),
                        "string",
                        context.packageName
                    )
                } ?: 0
            }
            if (resourceID == 0) return fallback
            return runCatching {
                if (args == null) context.getString(resourceID) else context.getString(
                    resourceID,
                    *args
                )
            }.getOrDefault(fallback)
        }

        private fun arguments(json: String?): Array<String>? = json?.let {
            runCatching {
                val values = JSONArray(it)
                Array(values.length()) { index -> values.getString(index) }
            }.getOrNull()
        }
    }
}
