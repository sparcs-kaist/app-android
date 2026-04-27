package org.sparcs.soap.App.Domain.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Usecases.FCMUseCaseProtocol
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmUseCase: FCMUseCaseProtocol

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            try {
                fcmUseCase.register(token)
            } catch (e: Exception) {
                Timber.e(e, "Registration failed")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            val data = remoteMessage.data
            if (data["type"] == "live_activity_update") {
                val step = data["step"]?.toIntOrNull() ?: 1
                val lectureName = data["lectureName"] ?: "수업 정보 없음"
                val nextLecture = data["nextLecture"] ?: "없음"
                val progress = data["progress"]?.toIntOrNull() ?: 0
                val location = data["location"] ?: data["place"] ?: "미지정"
                val kind = data["kind"] ?: ""
                val remainingMinutes = data["remainingMinutes"]?.toIntOrNull() ?: -1
                val remainingText = data["remainingText"] ?: ""

                val intent = Intent(this, LiveActivityService::class.java).apply {
                    putExtra("step", step)
                    putExtra("lectureName", lectureName)
                    putExtra("nextLecture", nextLecture)
                    putExtra("progress", progress)
                    putExtra("location", location)
                    putExtra("kind", kind)
                    putExtra("remainingMinutes", remainingMinutes)
                    putExtra("remainingText", remainingText)
                }

                ContextCompat.startForegroundService(this, intent)

                return
            }

            val title = remoteMessage.notification?.title ?: data["title"]
            val body = remoteMessage.notification?.body ?: data["body"]

            if (title != null && body != null) {
                showNotification(title, body)
            }

        } catch (e: Exception) {
            Timber.e(e, "Message processing failed")
        }
    }

    private fun showNotification(title: String, body: String) {
        try {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "buddy_notification_channel"

            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelName = "Buddy"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)

            val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_buddy_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Timber.e(e, "Notification display failed")
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}