package org.sparcs.soap.App.Domain.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import org.sparcs.soap.App.Features.Main.MainActivity
import org.sparcs.soap.R
import timber.log.Timber

class LiveActivityService : Service() {

    private val channelId = "live_activity_channel_v2"
    private val notificationId = 1001

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val step = intent?.getIntExtra("step", 1) ?: 1
        val lectureName = intent?.getStringExtra("lectureName") ?: "수업 정보 없음"
        val nextLecture = intent?.getStringExtra("nextLecture") ?: "없음"
        val progress = intent?.getIntExtra("progress", 0) ?: 0
        val location = intent?.getStringExtra("location")?.trim().orEmpty().ifBlank { "미지정" }
        val kind = intent?.getStringExtra("kind")?.trim().orEmpty().ifBlank { defaultKind(step) }
        val remainingMinutes = intent?.getIntExtra("remainingMinutes", -1) ?: -1
        val payloadRemainingText = intent?.getStringExtra("remainingText")?.trim().orEmpty()

        createNotificationChannel()

        val notification = buildCompatNotification(
            step = step,
            name = lectureName,
            next = nextLecture,
            progress = progress,
            location = location,
            kind = kind,
            remainingText = payloadRemainingText.ifBlank { formatRemainingText(remainingMinutes) },
            remainingMinutes = remainingMinutes
        )

        startForeground(notificationId, notification)

        return START_STICKY
    }

    private fun buildCompatNotification(
        step: Int,
        name: String,
        next: String,
        progress: Int,
        location: String,
        kind: String,
        remainingText: String,
        remainingMinutes: Int
    ): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } ?: Intent(this, MainActivity::class.java)

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, pendingIntentFlags)

        val displayTimeText = displayTimeText(step, next, remainingText)
        val expandedView = RemoteViews(packageName, R.layout.notification_live_activity_big)
        bindRemoteViews(
            views = expandedView,
            step = step,
            location = location,
            kind = kind,
            lectureName = name,
            timeText = displayTimeText,
            progress = progress,
            isDarkMode = isDarkModeEnabled()
        )

        if (Build.VERSION.SDK_INT >= 36) {
            return buildApi36Notification(
                pendingIntent = pendingIntent,
                expandedView = expandedView,
                title = name,
                content = displayTimeText,
                chipText = buildStatusChipText(location, remainingText),
                progress = progress,
                step = step,
                remainingMinutes = remainingMinutes
            )
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setCustomBigContentView(expandedView)
            .setCustomHeadsUpContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val publicVersion = NotificationCompat.Builder(this, channelId)
            .setContentTitle("수업 상태")
            .setContentText("수업 정보를 표시합니다")
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .build()
        builder.setPublicVersion(publicVersion)

        builder.setContentTitle(name)
            .setContentText(displayTimeText)

        if (step == 1) {
            builder.setProgress(0, 0, true)
        } else {
            builder.setProgress(100, progress.coerceIn(0, 100), false)
        }

        return builder.build()
    }

    private fun buildApi36Notification(
        pendingIntent: PendingIntent,
        expandedView: RemoteViews,
        title: String,
        content: String,
        chipText: String?,
        progress: Int,
        step: Int,
        remainingMinutes: Int
    ): Notification {
        val publicVersion = Notification.Builder(this, channelId)
            .setContentTitle("수업 상태")
            .setContentText("수업 정보를 표시합니다")
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .build()

        val builder = Notification.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setCustomBigContentView(expandedView)
            .setCustomHeadsUpContentView(expandedView)
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setContentTitle(title)
            .setContentText(content)
            .setPublicVersion(publicVersion)
            .setShowWhen(true)
            .setWhen(whenTimestampForChip(remainingMinutes))

        if (step == 1) {
            builder.setProgress(0, 0, true)
        } else {
            builder.setProgress(100, progress.coerceIn(0, 100), false)
        }

        setPromotedOngoingIfSupported(builder)
        if (!chipText.isNullOrBlank()) {
            setShortCriticalTextIfSupported(builder, chipText)
        }

        return builder.build()
    }

    private fun setPromotedOngoingIfSupported(builder: Notification.Builder) {
        if (!canPostPromotedNotificationsCompat()) {
            Timber.w("LiveActivity promoted ongoing skipped: canPostPromotedNotifications=false")
            return
        }
        try {
            builder.javaClass
                .getMethod("setRequestPromotedOngoing", Boolean::class.javaPrimitiveType)
                .invoke(builder, true)
            Timber.d("LiveActivity promoted ongoing requested")
        } catch (t: Throwable) {
            Timber.w(t, "LiveActivity promoted ongoing API unavailable on this build")
        }
    }

    private fun canPostPromotedNotificationsCompat(): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        return try {
            val manager = getSystemService(NotificationManager::class.java) ?: return false
            val method = manager.javaClass.getMethod("canPostPromotedNotifications")
            val result = method.invoke(manager) as? Boolean ?: false
            Timber.d("LiveActivity canPostPromotedNotifications=%s", result)
            result
        } catch (t: Throwable) {
            Timber.w(t, "LiveActivity canPostPromotedNotifications API unavailable")
            false
        }
    }

    private fun whenTimestampForChip(remainingMinutes: Int): Long {
        if (remainingMinutes <= 0) return System.currentTimeMillis()
        return System.currentTimeMillis() + remainingMinutes * 60_000L
    }

    private fun setShortCriticalTextIfSupported(builder: Notification.Builder, text: String) {
        try {
            builder.javaClass
                .getMethod("setShortCriticalText", String::class.java)
                .invoke(builder, text)
            Timber.d("LiveActivity shortCriticalText applied: %s", text)
        } catch (_: Throwable) {
            try {
                builder.javaClass
                    .getMethod("setShortCriticalText", CharSequence::class.java)
                    .invoke(builder, text)
                Timber.d("LiveActivity shortCriticalText(CharSequence) applied: %s", text)
            } catch (t: Throwable) {
                Timber.w(t, "LiveActivity shortCriticalText API unavailable on this build")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "실시간 수업 정보",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    description = "실시간 수업 상태를 잠금화면/헤드업으로 표시합니다."
                    setShowBadge(false)
                }
                manager?.createNotificationChannel(channel)
            }
        }
    }

    private fun bindRemoteViews(
        views: RemoteViews,
        step: Int,
        location: String,
        kind: String,
        lectureName: String,
        timeText: String,
        progress: Int,
        isDarkMode: Boolean
    ) {
        val backgroundColor = if (isDarkMode) 0xFF14161AL.toInt() else 0xFFF4F6FA.toInt()
        val primaryTextColor = if (isDarkMode) 0xFFFFFFFF.toInt() else 0xFF111827.toInt()
        val secondaryTextColor = if (isDarkMode) 0xFFE5E7EB.toInt() else 0xFF374151.toInt()

        views.setInt(R.id.live_root, "setBackgroundColor", backgroundColor)
        views.setTextViewText(R.id.live_top_left, location)
        views.setTextColor(R.id.live_top_left, primaryTextColor)
        views.setTextViewText(R.id.live_top_right, kind)
        views.setTextColor(R.id.live_top_right, secondaryTextColor)
        views.setTextViewText(R.id.live_title, lectureName)
        views.setTextColor(R.id.live_title, primaryTextColor)
        views.setTextViewText(R.id.live_time, timeText)
        views.setTextColor(R.id.live_time, secondaryTextColor)

        if (step == 1) {
            views.setProgressBar(R.id.live_progress, 0, 0, true)
        } else {
            views.setProgressBar(R.id.live_progress, 100, progress.coerceIn(0, 100), false)
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        val nightModeMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeMask == Configuration.UI_MODE_NIGHT_YES
    }

    private fun defaultKind(step: Int): String = when (step) {
        1 -> "SOON"
        2 -> "ON GOING"
        3 -> "UP NEXT"
        else -> "CLASS"
    }

    private fun displayTimeText(step: Int, nextLecture: String, remainingText: String): String = when (step) {
        1 -> if (remainingText.isBlank()) "곧 수업이 시작됩니다." else "시작까지 $remainingText"
        2 -> if (remainingText.isBlank()) "수업이 진행 중입니다." else "$remainingText 남음"
        3 -> "다음 수업: $nextLecture"
        else -> "수업 정보"
    }

    private fun formatRemainingText(remainingMinutes: Int): String {
        if (remainingMinutes <= 0) return ""
        val hours = remainingMinutes / 60
        val minutes = remainingMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun buildStatusChipText(location: String, remainingText: String): String? {
        val locationText = if (location.isBlank()) "" else "[$location]"
        val timeText = remainingText.trim()
        val combined = listOf(locationText, timeText).filter { it.isNotBlank() }.joinToString(" ")
        return when {
            combined.isBlank() -> null
            combined.length <= 18 -> combined
            locationText.isNotBlank() -> locationText
            else -> timeText.take(18)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null
}