package org.sparcs.soap.app.domain.services

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.services.notifications.TaxiNotifications
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * The number of Taxi chats you have not read yet, per room and in total, shared by every screen
 * that shows an unread badge: the Taxi tab, the Chats button and each room in the chat list.
 *
 * Seeded from the server's counts, then kept current from the chat socket while the app is open:
 * a chat from someone else in a room you are not looking at adds one, and opening a room clears
 * it, along with its notifications.
 */
@Singleton
class TaxiUnreadStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    // Resolved only once signed in, so the chat socket is not started by the sign-in screen.
    private val taxiChatServiceProvider: Provider<TaxiChatServiceProtocol>,
    private val userUseCase: UserUseCaseProtocol,
    private val authUseCaseProvider: Provider<AuthUseCaseProtocol>,
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Unread chats by room ID. Rooms with none may be missing. */
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    /** Unread chats across every room you are in, ongoing or done. */
    val totalCount: StateFlow<Int> = _counts
        .map { it.values.sum() }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    /** The room whose chat is on screen, whose chats are read as they arrive. */
    @Volatile
    var activeRoomId: String? = null
        private set

    // Unread chats are only fetched while signed in: an unauthorised request ends the session,
    // which would also cancel a sign-in that is under way.
    @Volatile
    private var isAuthenticated = false
    private var liveChatsJob: Job? = null

    init {
        observeAuthState()
        observeForeground()
    }

    /** Replaces the counts with the server's, such as when the app comes back to the foreground. */
    suspend fun refresh() {
        if (!isAuthenticated) return
        try {
            val (onGoing, done) = taxiRoomRepository.fetchMyRooms()
            _counts.value = (onGoing + done)
                .filter { it.id != activeRoomId }
                .associate { it.id to (it.unreadCount ?: 0) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "[TaxiUnreadStore] Failed to refresh unread chats")
        }
    }

    /** Marks [roomId] as the room on screen and clears its unread chats and notifications. */
    fun enterRoom(roomId: String) {
        activeRoomId = roomId
        markRead(roomId)
        TaxiNotifications.cancel(context, roomId)
    }

    /** Marks [roomId] as no longer on screen, if it still is. */
    fun leaveRoom(roomId: String) {
        if (activeRoomId == roomId) activeRoomId = null
    }

    /** Clears [roomId]'s unread chats, such as after replying to it from its notification. */
    fun markRead(roomId: String) {
        _counts.update { counts -> if ((counts[roomId] ?: 0) == 0) counts else counts + (roomId to 0) }
    }

    private fun observeAuthState() {
        scope.launch {
            authUseCaseProvider.get().isAuthenticatedFlow.collect { authenticated ->
                isAuthenticated = authenticated
                if (authenticated) {
                    startLiveChatsIfForeground()
                    refresh()
                } else {
                    _counts.value = emptyMap()
                    // The next person to sign in mustn't see the previous account's rides.
                    TaxiNotifications.clearAll(context)
                }
            }
        }
    }

    // The socket is only needed while Buddy is on screen, and starting it from a push or a reply
    // in the background would keep a connection open for nothing.
    private fun startLiveChatsIfForeground() {
        val isForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        if (isAuthenticated && isForeground && liveChatsJob == null) liveChatsJob = observeLiveChats()
    }

    private fun observeLiveChats(): Job =
        scope.launch {
            taxiChatServiceProvider.get().liveChatsPublisher.collect { chat ->
                val myId = runCatching { userUseCase.taxiUser?.oid }.getOrNull()
                if (!chat.isValid || (chat.authorID != null && chat.authorID == myId)) return@collect
                if (chat.roomID == activeRoomId) return@collect
                _counts.update { it + (chat.roomID to (it[chat.roomID] ?: 0) + 1) }
            }
        }

    // Pushes received while the app was in the background are only reflected in the server's
    // counts, so fetch them again whenever the app comes back.
    private fun observeForeground() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                startLiveChatsIfForeground()
                scope.launch { refresh() }
            }
        })
    }
}
