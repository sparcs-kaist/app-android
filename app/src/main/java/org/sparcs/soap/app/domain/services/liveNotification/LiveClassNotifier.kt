package org.sparcs.soap.app.domain.services.liveNotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.models.notification.LiveClassNotification
import timber.log.Timber
import kotlin.math.roundToInt

class LiveClassNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    fun handle(model: LiveClassNotification) {
        if (model.dismiss) {
            manager.cancel(model.notificationId)
            return
        }
        ensureChannel()
        try {
            val notification =
                if (Build.VERSION.SDK_INT >= 36) buildPromoted(model) else buildCompat(model)
            manager.notify(model.notificationId, notification)
        } catch (e: SecurityException) {
            Timber.w(e, "Live class notification post denied")
        }
    }

    private fun buildCompat(model: LiveClassNotification): Notification {
        val now = System.currentTimeMillis()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentTitle(model.title)
            .setContentText(contentText(model))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setCategory(NotificationCompat.CATEGORY_EVENT)

        model.countdownTargetMillis?.let { target ->
            builder.setWhen(target)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        }

        progressPercent(model, now)?.let { builder.setProgress(PROGRESS_MAX, it, false) }

        return builder.build()
    }

    @RequiresApi(36)
    private fun buildPromoted(model: LiveClassNotification): Notification {
        val now = System.currentTimeMillis()
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentTitle(model.title)
            .setContentText(contentText(model))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setRequestPromotedOngoing(true)
            .setColor(context.getColor(R.color.live_class_accent))

        model.countdownTargetMillis?.let { target ->
            builder.setWhen(target)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        }

        // Short text shown in the collapsed status-bar chip (e.g. "55m").
        shortChipText(model, now)?.let { builder.setShortCriticalText(it) }

        val style = Notification.ProgressStyle()
            .setProgressSegments(listOf(Notification.ProgressStyle.Segment(PROGRESS_MAX)))
            .setProgress(progressPercent(model, now) ?: 0)
        builder.style = style

        return builder.build()
    }

    private fun contentText(model: LiveClassNotification): String {
        val stateLabel = context.getString(model.state.labelRes)
        return model.location?.let { "$stateLabel · $it" } ?: stateLabel
    }

    private fun progressPercent(model: LiveClassNotification, now: Long): Int? =
        model.progress(now)?.let { (it * PROGRESS_MAX).roundToInt() }

    private fun shortChipText(model: LiveClassNotification, now: Long): String? {
        val target = model.countdownTargetMillis ?: return null
        val minutes = ((target - now).coerceAtLeast(0L) / 60_000L).toInt()
        return context.getString(R.string.live_class_minutes_short, minutes)
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, TIMETABLE_DEEP_LINK.toUri()).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            context,
            TIMETABLE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        val systemManager = context.getSystemService(NotificationManager::class.java) ?: return
        if (systemManager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.live_class_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.live_class_channel_description)
            enableVibration(false)
            setShowBadge(false)
        }
        systemManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "live_class_channel"
        private const val PROGRESS_MAX = 100
        private const val TIMETABLE_REQUEST_CODE = 5001
        private const val TIMETABLE_DEEP_LINK = "sparcsapp://otl/timetable"
    }
}
