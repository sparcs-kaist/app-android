package org.sparcs.soap.app.domain.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.enums.DeepLink
import org.sparcs.soap.app.domain.helpers.NotificationContent
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.services.notifications.TaxiNotifications
import org.sparcs.soap.app.domain.usecases.FCMUseCaseProtocol
import org.sparcs.soap.app.features.main.MainActivity
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmUseCase: FCMUseCaseProtocol

    @Inject
    lateinit var tokenStorage: TokenStorageProtocol

    // Only looked at while Buddy is on screen, so a push in the background doesn't create it.
    @Inject
    lateinit var taxiUnreadStore: Provider<TaxiUnreadStore>

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
        try {
            val content = NotificationContent.fromMessage(this, remoteMessage) ?: return
            // Signed out, this device may still be registered to the account, such as after
            // signing out offline or when the session expired, so its notifications aren't shown.
            // Ones sent to a topic aren't anyone's.
            val isForAccount = remoteMessage.from?.startsWith("/topics/") != true
            if (isForAccount && tokenStorage.getRefreshToken() == null) return
            if (TaxiNotifications.isTaxi(remoteMessage.data)) {
                // The open chat already shows the message.
                val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState
                    .isAtLeast(Lifecycle.State.STARTED)
                if (isForeground && taxiUnreadStore.get().activeRoomId == remoteMessage.data["room_id"]) return
                TaxiNotifications.show(this, remoteMessage.data, content)
                return
            }
            showNotification(content, remoteMessage)
        } catch (e: Exception) {
            Timber.e(e, "Message processing failed")
        }
    }

    private fun showNotification(content: NotificationContent, message: RemoteMessage) {
        try {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "buddy_notification_channel"

            val notificationID = message.messageId?.hashCode() ?: System.nanoTime().toInt()
            val destination = DeepLink.fromNotificationData(message.data)
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                message.data.forEach { (key, value) -> putExtra(key, value) }
                data = when (destination) {
                    is DeepLink.FeedPost -> destination.toUri()
                    null -> null
                    else -> (message.data["url"] ?: message.data["deep_link"])?.toUri()
                }
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                notificationID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelName = getString(R.string.app_name)
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
                .setContentTitle(content.title)
                .setContentText(content.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(notificationID, notification)
        } catch (e: Exception) {
            Timber.e(e, "Notification display failed")
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}