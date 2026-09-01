package org.sparcs.soap.app.domain.models.notification

import androidx.annotation.StringRes
import org.sparcs.soap.R

enum class LiveClassState(@get:StringRes val labelRes: Int) {
    STARTS_IN(R.string.live_class_starts_in),
    NOW(R.string.live_class_now),
    ON_GOING(R.string.live_class_on_going),
    ENDING(R.string.live_class_ending);

    val isOngoing: Boolean
        get() = this == ON_GOING || this == ENDING

    companion object {
        fun fromRaw(value: String?): LiveClassState? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

data class LiveClassNotification(
    val eventId: String,
    val title: String,
    val location: String?,
    val state: LiveClassState,
    val startEpochMillis: Long?,
    val endEpochMillis: Long?,
    val dismiss: Boolean,
) {
    val notificationId: Int
        get() = eventId.hashCode()

    val countdownTargetMillis: Long?
        get() = if (state.isOngoing) endEpochMillis else startEpochMillis

    fun progress(nowMillis: Long): Float? {
        if (!state.isOngoing) return null
        val start = startEpochMillis ?: return null
        val end = endEpochMillis ?: return null
        if (end <= start) return null
        return ((nowMillis - start).toFloat() / (end - start).toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        const val TYPE = "live_class"

        fun fromData(data: Map<String, String>): LiveClassNotification? {
            if (data["type"] != TYPE) return null

            val eventId = data["event_id"]?.takeIf { it.isNotBlank() } ?: return null
            val dismiss = data["dismiss"]?.equals("true", ignoreCase = true) == true

            val state = LiveClassState.fromRaw(data["state"])
            if (!dismiss) {
                if (state == null) return null
                if (data["title"].isNullOrBlank()) return null
            }

            return LiveClassNotification(
                eventId = eventId,
                title = data["title"].orEmpty(),
                location = data["location"]?.takeIf { it.isNotBlank() },
                state = state ?: LiveClassState.STARTS_IN,
                startEpochMillis = data["start_epoch"]?.toLongOrNull(),
                endEpochMillis = data["end_epoch"]?.toLongOrNull(),
                dismiss = dismiss,
            )
        }
    }
}
