package org.sparcs.soap.app.domain.services.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiChatRequest
import org.sparcs.soap.app.domain.repositories.taxi.TaxiChatRepositoryProtocol
import org.sparcs.soap.app.domain.services.TaxiUnreadStore
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

/** Sends a reply typed into a Taxi notification, without opening the app. */
@AndroidEntryPoint
class TaxiReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taxiChatRepository: TaxiChatRepositoryProtocol

    @Inject
    lateinit var taxiUnreadStore: Provider<TaxiUnreadStore>

    override fun onReceive(context: Context, intent: Intent) {
        val roomId = intent.getStringExtra(TaxiNotifications.EXTRA_ROOM_ID) ?: return
        val roomName = intent.getStringExtra(TaxiNotifications.EXTRA_ROOM_NAME).orEmpty()
        val text = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(TaxiNotifications.KEY_REPLY)
            ?.toString()
            ?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return

        // Kept alive until the reply has been sent.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                taxiChatRepository.sendChat(TaxiChatRequest(roomId, TaxiChat.ChatType.TEXT, text))
                // Replying means you have read the room; failing to mark it only leaves its badge.
                runCatching { taxiChatRepository.readChats(roomId) }
                withContext(Dispatchers.Main) { taxiUnreadStore.get().markRead(roomId) }
                TaxiNotifications.appendReply(context, roomId, roomName, text)
            } catch (e: Exception) {
                Timber.e(e, "[TaxiReplyReceiver] Failed to send a quick reply")
                TaxiNotifications.showReplyFailed(context, roomId, roomName)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
