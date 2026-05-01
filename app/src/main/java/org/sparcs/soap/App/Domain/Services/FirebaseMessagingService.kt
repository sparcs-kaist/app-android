package org.sparcs.soap.App.Domain.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Usecases.FCMUseCaseProtocol
import org.sparcs.soap.R
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmUseCase: FCMUseCaseProtocol

    @Inject
    lateinit var tokenStorage: TokenStorageProtocol

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            if (tokenStorage.getRefreshToken() != null) {
                try {
                    fcmUseCase.register(token)
                } catch (e: Exception) {
                    Timber.e(e, "Registration failed")
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // TODO: 알람 기능 도입 시 아래 코드 활성화
//        return
        super.onMessageReceived(remoteMessage)
        try {
            var title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
            val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

            val locKey = remoteMessage.notification?.titleLocalizationKey ?: remoteMessage.data["title_loc_key"]

            if (locKey != null) {
                val resId = resources.getIdentifier(locKey.lowercase(), "string", packageName)
                if (resId != 0) {
                    title = getString(resId)
                }
            }
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
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            }

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