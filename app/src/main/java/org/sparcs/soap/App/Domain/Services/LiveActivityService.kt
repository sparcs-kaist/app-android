package org.sparcs.soap.App.Domain.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.sparcs.soap.App.Features.Main.MainActivity
import org.sparcs.soap.R

class LiveActivityService : Service() {

    private val channelId = "live_activity_channel"
    private val notificationId = 1001

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val step = intent?.getIntExtra("step", 1) ?: 1
        val lectureName = intent?.getStringExtra("lectureName") ?: "수업 정보 없음"
        val nextLecture = intent?.getStringExtra("nextLecture") ?: "없음"
        val progress = intent?.getIntExtra("progress", 0) ?: 0

        createNotificationChannel()

        val notification = buildCompatNotification(step, lectureName, nextLecture, progress)

        startForeground(notificationId, notification)

        return START_STICKY
    }

    private fun buildCompatNotification(step: Int, name: String, next: String, progress: Int): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } ?: Intent(this, MainActivity::class.java)

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else
            PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        val publicVersion = NotificationCompat.Builder(this, channelId)
            .setContentTitle("수업 상태")
            .setContentText("수업 정보를 표시합니다")
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .build()
        builder.setPublicVersion(publicVersion)

        when (step) {
            1 -> {
                builder.setContentTitle("$name 시작 전")
                    .setContentText("곧 수업이 시작됩니다.")
                    .setProgress(0, 0, true)
            }
            2 -> {
                builder.setContentTitle(name)
                    .setContentText("수업이 진행 중입니다.")
                    .setProgress(100, progress.coerceIn(0, 100), false)
            }
            3 -> {
                builder.setContentTitle(name)
                    .setContentText("다음 수업: $next")
                    .setProgress(100, progress.coerceIn(0, 100), false)
            }
            else -> {
                builder.setContentTitle(name)
                    .setContentText("수업 정보")
            }
        }

        val notification = builder.build()
        notification.flags = notification.flags or 0x00040000
        return notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "실시간 수업 정보",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                manager?.createNotificationChannel(channel)
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null
}