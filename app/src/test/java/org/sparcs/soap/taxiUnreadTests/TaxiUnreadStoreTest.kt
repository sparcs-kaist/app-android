package org.sparcs.soap.taxiUnreadTests

import android.app.Activity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.services.AppForegroundMonitor
import org.sparcs.soap.app.domain.services.MockTaxiChatService
import org.sparcs.soap.app.domain.services.TaxiChatServiceProtocol
import org.sparcs.soap.app.domain.services.TaxiUnreadStore
import org.sparcs.soap.app.domain.services.notifications.TaxiNotificationCleaner
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.shared.mocks.taxi.mock
import org.sparcs.soap.buddyTestSupport.repository.MockTaxiRoomRepository
import org.sparcs.soap.buddyTestSupport.useCase.MockUserUseCase
import org.sparcs.soap.testSupport.MainDispatcherRule
import java.util.Date

class TaxiUnreadStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val isAuthenticated = MutableStateFlow(true)
    private val isForeground = MutableStateFlow(true)
    private val liveChats = MutableSharedFlow<TaxiChat>()
    private var serverCounts = mapOf("ride" to 2)
    private var fetches = 0
    /** While set, fetching the rooms waits for it. */
    private var fetchGate: CompletableDeferred<Unit>? = null
    private var clearedAll = 0

    private val rooms = object : TaxiRoomRepositoryProtocol by MockTaxiRoomRepository() {
        override suspend fun fetchMyRooms(): Pair<List<TaxiRoom>, List<TaxiRoom>> {
            fetches += 1
            fetchGate?.await()
            return serverCounts.map { (id, count) -> TaxiRoom.mock().copy(id = id, unreadCount = count) } to emptyList()
        }
    }

    private val chatService = object : TaxiChatServiceProtocol by MockTaxiChatService() {
        override val liveChatsPublisher: Flow<TaxiChat> = liveChats
    }

    private val auth = object : AuthUseCaseProtocol {
        override val isAuthenticatedFlow: Flow<Boolean> = isAuthenticated
        override suspend fun signIn(activity: Activity) {}
        override suspend fun signOut() {}
        override fun getAccessToken(): String? = null
        override suspend fun getValidAccessToken(): String = ""
        override suspend fun refreshAccessToken(force: Boolean) {}
    }

    private fun store() = TaxiUnreadStore(
        taxiRoomRepository = rooms,
        taxiChatServiceProvider = { chatService },
        userUseCase = MockUserUseCase(),
        authUseCaseProvider = { auth },
        foregroundMonitor = object : AppForegroundMonitor {
            override val isForeground = this@TaxiUnreadStoreTest.isForeground
        },
        notifications = object : TaxiNotificationCleaner {
            override fun cancel(roomId: String) {}
            override fun clearAll() { clearedAll += 1 }
        },
    )

    private suspend fun chatIn(roomId: String) = liveChats.emit(
        TaxiChat(
            roomID = roomId, type = TaxiChat.ChatType.TEXT, authorID = "someone else",
            authorName = null, authorProfileURL = null, authorIsWithdrew = false,
            content = "hi", time = Date(), isValid = true, inOutNames = null,
        )
    )

    @Test
    fun `opening the app fetches the counts once`() {
        val store = store()

        assertEquals(1, fetches)
        assertEquals(2, store.totalCount.value)
    }

    @Test
    fun `counts fetched for a session that has ended are dropped`() {
        isForeground.value = false
        fetchGate = CompletableDeferred()
        val store = store()

        isAuthenticated.value = false
        fetchGate!!.complete(Unit)

        assertEquals(emptyMap<String, Int>(), store.counts.value)
        assertTrue(clearedAll > 0)
    }

    @Test
    fun `a chat that arrives during a refresh isn't lost`() = kotlinx.coroutines.test.runTest {
        serverCounts = mapOf("ride" to 0)
        val store = store()
        isForeground.value = false
        fetchGate = CompletableDeferred()

        isForeground.value = true
        chatIn("ride")
        fetchGate!!.complete(Unit)

        assertEquals(1, store.counts.value["ride"])
    }

    @Test
    fun `a chat the server already counted isn't counted twice`() = kotlinx.coroutines.test.runTest {
        serverCounts = mapOf("ride" to 0)
        val store = store()
        isForeground.value = false
        fetchGate = CompletableDeferred()

        isForeground.value = true
        chatIn("ride")
        serverCounts = mapOf("ride" to 1)
        fetchGate!!.complete(Unit)

        assertEquals(1, store.counts.value["ride"])
    }

    @Test
    fun `chats stop counting once you sign out`() = kotlinx.coroutines.test.runTest {
        val store = store()

        isAuthenticated.value = false
        chatIn("ride")

        assertEquals(0, store.totalCount.value)
    }

    @Test
    fun `nothing is fetched or listened to while Buddy is in the background`() = kotlinx.coroutines.test.runTest {
        isForeground.value = false
        val store = store()

        chatIn("ride")

        assertEquals(0, fetches)
        assertEquals(0, store.totalCount.value)
        assertEquals(0, liveChats.subscriptionCount.value)
    }
}
