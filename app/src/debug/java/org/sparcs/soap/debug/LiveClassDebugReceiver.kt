package org.sparcs.soap.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.sparcs.soap.app.domain.models.notification.LiveClassNotification
import org.sparcs.soap.app.domain.models.notification.LiveClassState
import org.sparcs.soap.app.domain.services.liveNotification.LiveClassNotifier

class LiveClassDebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifier = LiveClassNotifier(context.applicationContext)
        val eventId = intent.getStringExtra("event_id") ?: "debug-live-class"
        val dismiss = intent.getBooleanExtra("dismiss", false)

        if (dismiss) {
            notifier.handle(
                LiveClassNotification(
                    eventId = eventId,
                    title = "",
                    location = null,
                    state = LiveClassState.ON_GOING,
                    startEpochMillis = null,
                    endEpochMillis = null,
                    dismiss = true,
                )
            )
            return
        }

        val state = LiveClassState.fromRaw(intent.getStringExtra("state")) ?: LiveClassState.ON_GOING
        val now = System.currentTimeMillis()
        val startOff = intent.getLongExtra("start_off", -20L)
        val endOff = intent.getLongExtra("end_off", 55L)

        notifier.handle(
            LiveClassNotification(
                eventId = eventId,
                title = intent.getStringExtra("title") ?: "System Programming",
                location = intent.getStringExtra("location") ?: "E11 304",
                state = state,
                startEpochMillis = now + startOff * 60_000L,
                endEpochMillis = now + endOff * 60_000L,
                dismiss = false,
            )
        )
    }
}
