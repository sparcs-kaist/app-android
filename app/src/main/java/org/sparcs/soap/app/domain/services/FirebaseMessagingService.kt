package org.sparcs.soap.app.domain.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.notification.LiveClassNotification
import org.sparcs.soap.app.domain.services.liveNotification.LiveClassNotifier
import org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol
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
        super.onMessageReceived(remoteMessage)

        LiveClassNotification.fromData(remoteMessage.data)?.let { liveClass ->
            LiveClassNotifier(this).handle(liveClass)
            return
        }

        try {
            val title = getLocalizedString(
                remoteMessage.notification?.titleLocalizationKey ?: remoteMessage.data["title_loc_key"],
                remoteMessage.notification?.title ?: remoteMessage.data["title"]
            )
            val body = getLocalizedString(
                remoteMessage.notification?.bodyLocalizationKey ?: remoteMessage.data["body_loc_key"],
                remoteMessage.notification?.body ?: remoteMessage.data["body"]
            )

            if (title != null && body != null) {
                showNotification(title, body)
            }
        } catch (e: Exception) {
            Timber.e(e, "Message processing failed")
        }
    }

    private fun getLocalizedString(locKey: String?, defaultValue: String?): String? {
        return locKey?.lowercase()?.let { key ->
            val resId = resources.getIdentifier(key, "string", packageName)
            if (resId != 0) getString(resId) else null
        } ?: defaultValue
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