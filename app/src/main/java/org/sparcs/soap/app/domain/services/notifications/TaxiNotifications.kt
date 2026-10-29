package org.sparcs.soap.app.domain.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.withTimeoutOrNull
import org.sparcs.soap.R
import org.sparcs.soap.app.domain.helpers.NotificationContent
import org.sparcs.soap.app.features.main.MainActivity
import timber.log.Timber
import java.io.File
import java.time.Instant

/**
 * Taxi chat and ride notifications, which the Taxi server sends through Buddy's backend as data
 * messages with the room in their data.
 *
 * Chats are shown as the ride's group conversation, one notification per room that collects its
 * messages, each with the sender's name and photo, and linked to a long-lived shortcut so it is
 * listed among the system's conversations. Ride events, such as the departure reminder, are shown
 * on their own with the ride's name as the title. Both can be replied to.
 */
object TaxiNotifications {
    const val CHATS_CHANNEL_ID = "taxi_chats"
    const val UPDATES_CHANNEL_ID = "taxi_updates"

    const val KEY_REPLY = "taxi_reply"
    const val EXTRA_ROOM_ID = "room_id"
    const val EXTRA_ROOM_NAME = "room_name"

    private const val CHAT_TAG = "taxi_chat"
    private const val EVENT_TAG = "taxi_event"
    private const val ME_KEY = "me"
    private const val SHORTCUT_CATEGORY = "org.sparcs.soap.category.TAXI_CHAT"
    private const val IMAGES_DIRECTORY = "notification_images"
    private const val IMAGE_LIFETIME_MILLIS = 24 * 60 * 60 * 1000L
    private const val IMAGE_TIMEOUT_MILLIS = 5_000L
    private const val AVATAR_SIZE = 256
    private const val PHOTO_SIZE = 1440

    /** Whether [data] is a Taxi notification's. */
    fun isTaxi(data: Map<String, String>): Boolean =
        data["category"]?.startsWith("taxi_") == true && !data["room_id"].isNullOrBlank()

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHATS_CHANNEL_ID,
                    context.getString(R.string.taxi_chats_channel),
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    UPDATES_CHANNEL_ID,
                    context.getString(R.string.taxi_updates_channel),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        )
    }

    /** Shows a Taxi push. Downloads images, so call it off the main thread. */
    internal fun show(context: Context, data: Map<String, String>, content: NotificationContent) {
        if (!canNotify(context)) return
        createChannels(context)
        val roomId = data["room_id"] ?: return
        val roomName = data["room_name"] ?: content.title

        if (data["category"] == "taxi_chat") {
            showChat(context, data, roomId, roomName, content.body)
        } else {
            showEvent(context, roomId, roomName, content)
        }
    }

    /** Adds your reply to the room's conversation, which also ends the reply action's spinner. */
    fun appendReply(context: Context, roomId: String, roomName: String, text: String) {
        val style = existingStyle(context, roomId) ?: newStyle(context, roomName)
        style.addMessage(NotificationCompat.MessagingStyle.Message(text, System.currentTimeMillis(), null as Person?))
        notifyChat(context, roomId, roomName, style, alert = false)
        // A reply from a ride event's notification is now part of the conversation.
        NotificationManagerCompat.from(context).cancel(EVENT_TAG, roomId.hashCode())
    }

    /** Says that a reply could not be sent, keeping the reply action so it can be sent again. */
    fun showReplyFailed(context: Context, roomId: String, roomName: String) {
        if (!canNotify(context)) return
        val notification = NotificationCompat.Builder(context, CHATS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentTitle(roomName)
            .setContentText(context.getString(R.string.taxi_reply_failed))
            .setContentIntent(openChatIntent(context, roomId))
            .addAction(replyAction(context, roomId, roomName))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(EVENT_TAG, roomId.hashCode(), notification)
    }

    /**
     * Removes everything Taxi notifications leave behind: their notifications, the rides'
     * conversation shortcuts and the saved photos, such as once you sign out.
     */
    fun clearAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.activeNotifications
            .filter { it.tag == CHAT_TAG || it.tag == EVENT_TAG }
            .forEach { manager.cancel(it.tag, it.id) }
        runCatching {
            val rideShortcuts = ShortcutManagerCompat
                .getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_DYNAMIC or ShortcutManagerCompat.FLAG_MATCH_CACHED)
                .filter { SHORTCUT_CATEGORY in it.categories.orEmpty() }
                .map { it.id }
            ShortcutManagerCompat.removeLongLivedShortcuts(context, rideShortcuts)
        }.onFailure { Timber.e(it, "[TaxiNotifications] Failed to remove the rides' shortcuts") }
        File(context.cacheDir, IMAGES_DIRECTORY).deleteRecursively()
    }

    /** Removes the room's notifications, such as once its chat is on screen. */
    fun cancel(context: Context, roomId: String) {
        val manager = NotificationManagerCompat.from(context)
        manager.cancel(CHAT_TAG, roomId.hashCode())
        manager.cancel(EVENT_TAG, roomId.hashCode())
    }

    private fun showChat(
        context: Context,
        data: Map<String, String>,
        roomId: String,
        roomName: String,
        body: String,
    ) {
        val senderId = data["sender_id"] ?: roomId
        val sender = Person.Builder()
            .setKey(senderId)
            .setName(data["sender_name"] ?: roomName)
            .apply { loadBitmap(context, data["sender_avatar_url"], AVATAR_SIZE, circle = true)?.let { setIcon(IconCompat.createWithBitmap(it)) } }
            .build()

        val sentAt = data["sent_at"]?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: System.currentTimeMillis()
        val message = NotificationCompat.MessagingStyle.Message(body, sentAt, sender)
        imageUri(context, data["image_url"], data["chat_id"] ?: sentAt.toString())?.let {
            message.setData("image/jpeg", it)
        }

        val style = existingStyle(context, roomId) ?: newStyle(context, roomName)
        style.addMessage(message)

        pushShortcut(context, roomId, roomName, sender)
        notifyChat(context, roomId, roomName, style, alert = true)
    }

    private fun showEvent(context: Context, roomId: String, roomName: String, content: NotificationContent) {
        val notification = NotificationCompat.Builder(context, UPDATES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setContentTitle(roomName)
            .setContentText(content.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(openChatIntent(context, roomId))
            .addAction(replyAction(context, roomId, roomName))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(EVENT_TAG, roomId.hashCode(), notification)
    }

    private fun notifyChat(
        context: Context,
        roomId: String,
        roomName: String,
        style: NotificationCompat.MessagingStyle,
        alert: Boolean,
    ) {
        if (!canNotify(context)) return
        val notification = NotificationCompat.Builder(context, CHATS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_buddy_notification)
            .setStyle(style)
            .setShortcutId(roomId)
            .setLocusId(LocusIdCompat(roomId))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(openChatIntent(context, roomId))
            .addAction(replyAction(context, roomId, roomName))
            .setNumber(style.messages.count { it.person != null })
            .setOnlyAlertOnce(!alert)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(CHAT_TAG, roomId.hashCode(), notification)
    }

    private fun newStyle(context: Context, roomName: String): NotificationCompat.MessagingStyle {
        val me = Person.Builder().setKey(ME_KEY).setName(context.getString(R.string.taxi_notification_me)).build()
        return NotificationCompat.MessagingStyle(me)
            .setConversationTitle(roomName)
            .setGroupConversation(true)
    }

    /** The room's conversation so far, so a new message is added to it rather than replacing it. */
    private fun existingStyle(context: Context, roomId: String): NotificationCompat.MessagingStyle? {
        val manager = context.getSystemService(NotificationManager::class.java)
        val active = manager.activeNotifications.firstOrNull {
            it.tag == CHAT_TAG && it.id == roomId.hashCode()
        } ?: return null
        return NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(active.notification)
    }

    private fun replyAction(context: Context, roomId: String, roomName: String): NotificationCompat.Action {
        val intent = Intent(context, TaxiReplyReceiver::class.java)
            .putExtra(EXTRA_ROOM_ID, roomId)
            .putExtra(EXTRA_ROOM_NAME, roomName)
        // Mutable, since the system adds the typed reply to it.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(KEY_REPLY)
            .setLabel(context.getString(R.string.taxi_reply))
            .build()
        return NotificationCompat.Action.Builder(R.drawable.ic_reply, context.getString(R.string.taxi_reply), pendingIntent)
            .addRemoteInput(remoteInput)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .setShowsUserInterface(false)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun openChatIntent(context: Context, roomId: String): PendingIntent =
        PendingIntent.getActivity(
            context,
            roomId.hashCode(),
            openChat(context, roomId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** Opens the room's chat, read by [org.sparcs.soap.app.domain.enums.DeepLink.fromIntent]. */
    private fun openChat(context: Context, roomId: String): Intent =
        Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra("category", "taxi_chat")
            .putExtra(EXTRA_ROOM_ID, roomId)

    /** A long-lived shortcut for the room, which makes its notification a conversation. */
    private fun pushShortcut(context: Context, roomId: String, roomName: String, sender: Person) {
        val shortcut = ShortcutInfoCompat.Builder(context, roomId)
            .setLongLived(true)
            .setShortLabel(roomName)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(openChat(context, roomId))
            .setLocusId(LocusIdCompat(roomId))
            .setPerson(sender)
            .setCategories(setOf(SHORTCUT_CATEGORY))
            .build()
        runCatching { ShortcutManagerCompat.pushDynamicShortcut(context, shortcut) }
            .onFailure { Timber.e(it, "[TaxiNotifications] Failed to push the room's shortcut") }
    }

    /**
     * Downloads an image from the push, at most [size] pixels on its longer side. Only web URLs are
     * loaded, since the URL comes from the push, and a slow one is given up on so the notification
     * still shows.
     */
    private fun loadBitmap(context: Context, url: String?, size: Int, circle: Boolean): Bitmap? {
        val uri = url?.let { runCatching { Uri.parse(it) }.getOrNull() } ?: return null
        if (uri.scheme?.lowercase() !in setOf("https", "http")) return null
        return runCatching {
            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(size)
                .allowHardware(false)
                .apply { if (circle) transformations(CircleCropTransformation()) }
                .build()
            val result = kotlinx.coroutines.runBlocking {
                withTimeoutOrNull(IMAGE_TIMEOUT_MILLIS) { context.imageLoader.execute(request) }
            }
            (result as? SuccessResult)?.drawable?.toBitmap()
        }.getOrNull()
    }

    /** A photo sent in the chat, saved where the system's notification UI may read it. */
    private fun imageUri(context: Context, url: String?, name: String): Uri? {
        val bitmap = loadBitmap(context, url, PHOTO_SIZE, circle = false) ?: return null
        return runCatching {
            val directory = File(context.cacheDir, IMAGES_DIRECTORY).apply { mkdirs() }
            // Photos older than a day have long left the notification shade.
            val expired = System.currentTimeMillis() - IMAGE_LIFETIME_MILLIS
            directory.listFiles()?.filter { it.lastModified() < expired }?.forEach { it.delete() }
            // The name comes from the push, so only letters, digits, '-' and '_' are kept, which
            // can't lead outside the directory.
            val safeName = name.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.take(64).ifEmpty { "photo" }
            val file = File(directory, "$safeName.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri
        }.getOrNull()
    }

    private fun canNotify(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
