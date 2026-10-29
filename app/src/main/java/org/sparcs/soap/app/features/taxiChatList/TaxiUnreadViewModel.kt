package org.sparcs.soap.app.features.taxiChatList

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.sparcs.soap.app.domain.services.TaxiUnreadStore
import javax.inject.Inject

/** Unread Taxi chats for the screens that show their badges, and the chat that is on screen. */
@HiltViewModel
class TaxiUnreadViewModel @Inject constructor(
    private val taxiUnreadStore: TaxiUnreadStore
) : ViewModel() {
    val counts: StateFlow<Map<String, Int>> = taxiUnreadStore.counts
    val totalCount: StateFlow<Int> = taxiUnreadStore.totalCount

    fun enterRoom(roomId: String) = taxiUnreadStore.enterRoom(roomId)

    fun leaveRoom(roomId: String) = taxiUnreadStore.leaveRoom(roomId)
}
