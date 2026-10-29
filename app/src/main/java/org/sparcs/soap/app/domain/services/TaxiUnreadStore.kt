package org.sparcs.soap.app.domain.services

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.services.notifications.TaxiNotificationCleaner
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface TaxiUnreadStoreProtocol {
    /** Unread chats by room ID. Rooms with none may be missing. */
    val counts: StateFlow<Map<String, Int>>
    /** Unread chats across every room you are in, ongoing or done. */
    val totalCount: StateFlow<Int>
    /** The room whose chat is on screen, whose chats are read as they arrive. */
    val activeRoomId: String?

    /** Marks [roomId] as the room on screen and clears its unread chats and notifications. */
    fun enterRoom(roomId: String)
    /** Marks [roomId] as no longer on screen, if it still is. */
    fun leaveRoom(roomId: String)
    /** Clears [roomId]'s unread chats, such as after replying to it from its notification. */
    fun markRead(roomId: String)
}

class MockTaxiUnreadStore(counts: Map<String, Int> = emptyMap()) : TaxiUnreadStoreProtocol {
    override val counts = MutableStateFlow(counts)
    override val totalCount: StateFlow<Int> = MutableStateFlow(counts.values.sum())
    override var activeRoomId: String? = null
    override fun enterRoom(roomId: String) { activeRoomId = roomId }
    override fun leaveRoom(roomId: String) { if (activeRoomId == roomId) activeRoomId = null }
    override fun markRead(roomId: String) {}
}

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
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
    // Resolved only once signed in, so the chat socket is not started by the sign-in screen.
    private val taxiChatServiceProvider: Provider<TaxiChatServiceProtocol>,
    private val userUseCase: UserUseCaseProtocol,
    private val authUseCaseProvider: Provider<AuthUseCaseProtocol>,
    private val foregroundMonitor: AppForegroundMonitor,
    private val notifications: TaxiNotificationCleaner,
) : TaxiUnreadStoreProtocol {
    // Everything below runs on the main thread, except `markRead`, which may be called from a
    // notification's reply and only updates `_counts`.
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    override val totalCount: StateFlow<Int> = _counts
        .map { it.values.sum() }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    @Volatile
    override var activeRoomId: String? = null
        private set

    // Unread chats are only fetched while signed in: an unauthorised request ends the session,
    // which would also cancel a sign-in that is under way.
    private var isAuthenticated = false
    // Changes whenever you sign in or out, so counts fetched for a session that has ended are dropped.
    private var session = 0
    // Whether you're signed in with Buddy on screen, as of the last change to either.
    private var isActive = false
    private var liveChatsJob: Job? = null
    private var refreshJob: Job? = null
    // Rooms that got chats on the socket while their counts were being fetched.
    private var roomsChangedDuringRefresh: MutableSet<String>? = null

    init {
        observeSession()
    }

    override fun enterRoom(roomId: String) {
        activeRoomId = roomId
        markRead(roomId)
        notifications.cancel(roomId)
    }

    override fun leaveRoom(roomId: String) {
        if (activeRoomId == roomId) activeRoomId = null
    }

    override fun markRead(roomId: String) {
        _counts.update { counts -> if ((counts[roomId] ?: 0) == 0) counts else counts + (roomId to 0) }
    }

    /** Replaces the counts with the server's. */
    private fun refresh() {
        if (!isAuthenticated || refreshJob?.isActive == true) return
        val refreshedSession = session
        refreshJob = scope.launch {
            val changedRooms = mutableSetOf<String>().also { roomsChangedDuringRefresh = it }
            try {
                val (onGoing, done) = taxiRoomRepository.fetchMyRooms()
                // Signed out, or in as someone else, while they were being fetched.
                if (session != refreshedSession) return@launch
                val current = _counts.value
                _counts.value = (onGoing + done)
                    .filter { it.id != activeRoomId }
                    .associate { room ->
                        val fetched = room.unreadCount ?: 0
                        // A chat that arrived meanwhile may not be in the server's count yet.
                        val count = if (room.id in changedRooms) maxOf(fetched, current[room.id] ?: 0) else fetched
                        room.id to count
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "[TaxiUnreadStore] Failed to refresh unread chats")
            } finally {
                if (roomsChangedDuringRefresh === changedRooms) roomsChangedDuringRefresh = null
            }
        }
    }

    // The counts are fetched, and chats listened to, while you're signed in and Buddy is on screen:
    // once as it opens, when you sign in, and whenever it comes back, as pushes received in the
    // background are only reflected in the server's counts. The socket would otherwise keep a
    // connection open for nothing, such as when started by a push or a reply in the background.
    private fun observeSession() {
        scope.launch {
            combine(authUseCaseProvider.get().isAuthenticatedFlow, foregroundMonitor.isForeground, ::Pair)
                .collect { (authenticated, foreground) ->
                    // The first state is handled even when signed out, to clear what a past session left.
                    if (authenticated != isAuthenticated || session == 0) changeSession(authenticated)
                    val active = authenticated && foreground
                    if (active && !isActive) {
                        if (liveChatsJob == null) liveChatsJob = observeLiveChats()
                        refresh()
                    }
                    isActive = active
                }
        }
    }

    private fun changeSession(authenticated: Boolean) {
        isAuthenticated = authenticated
        session += 1
        refreshJob?.cancel()
        if (!authenticated) {
            liveChatsJob?.cancel()
            liveChatsJob = null
            _counts.value = emptyMap()
            // The next person to sign in mustn't see the previous account's rides.
            notifications.clearAll()
        }
    }

    private fun observeLiveChats(): Job =
        scope.launch {
            taxiChatServiceProvider.get().liveChatsPublisher.collect { chat ->
                if (!isAuthenticated) return@collect
                val myId = runCatching { userUseCase.taxiUser?.oid }.getOrNull()
                if (!chat.isValid || (chat.authorID != null && chat.authorID == myId)) return@collect
                if (chat.roomID == activeRoomId) return@collect
                roomsChangedDuringRefresh?.add(chat.roomID)
                _counts.update { it + (chat.roomID to (it[chat.roomID] ?: 0) + 1) }
            }
        }
}
