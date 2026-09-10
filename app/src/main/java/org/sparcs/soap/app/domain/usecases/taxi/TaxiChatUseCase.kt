package org.sparcs.soap.app.domain.usecases.taxi

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.models.taxi.TaxiChatRequest
import org.sparcs.soap.app.domain.models.taxi.TaxiRoom
import org.sparcs.soap.app.domain.repositories.taxi.TaxiChatRepositoryProtocol
import org.sparcs.soap.app.domain.repositories.taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.app.domain.services.TaxiChatService
import org.sparcs.soap.app.domain.usecases.UserUseCaseProtocol
import org.sparcs.soap.app.shared.extensions.toByteArray
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

interface TaxiChatUseCaseProtocol {
    val chats: StateFlow<List<TaxiChat>>
    val roomUpdateFlow: Flow<TaxiRoom>
    val accountChats: List<TaxiChat>

    fun setRoom(room: TaxiRoom)
    fun reconnect()
    suspend fun fetchInitialChats()
    suspend fun fetchChats(before: Date)
    suspend fun sendChat(content: String?, type: TaxiChat.ChatType)
    suspend fun sendImage(content: Bitmap)
    fun switchRoom(newRoomId: String)
    suspend fun refreshRoom()
}

class TaxiChatUseCase @Inject constructor(
    private val taxiChatService: TaxiChatService,
    private val userUseCase: UserUseCaseProtocol,
    private val taxiChatRepository: TaxiChatRepositoryProtocol,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
) : TaxiChatUseCaseProtocol {

    private lateinit var room: TaxiRoom
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // MARK: - Flows
    private val _chats = MutableStateFlow<List<TaxiChat>>(emptyList())
    override val chats: StateFlow<List<TaxiChat>> = _chats.asStateFlow()

    private val _roomUpdateFlow = MutableSharedFlow<TaxiRoom>()
    override val roomUpdateFlow: Flow<TaxiRoom> = _roomUpdateFlow.asSharedFlow()

    private var isSocketConnected: Boolean = false
    private var hasInitialChatsBeenFetched: Boolean = false
    private var flatChats: List<TaxiChat> = emptyList()

    // MARK: - Computed Properties
    override var accountChats: List<TaxiChat> = emptyList()
    private var lastReadChatId: UUID? = null
    private var isFirstReadSent = false

    private var isBound = false
    private var bindJob: Job? = null
    private var departureRefreshJob: Job? = null

    override fun setRoom(room: TaxiRoom) {
        this.room = room
        this.flatChats = emptyList()
        this.isFirstReadSent = false
        this.hasInitialChatsBeenFetched = false
        taxiChatService.setRoom(room.id)
        scheduleDepartureRefresh()
    }

    private fun scheduleDepartureRefresh() {
        departureRefreshJob?.cancel()
        val now = Date()
        val delayMs = room.departAt.time - now.time
        if (delayMs > 0) {
            departureRefreshJob = scope.launch {
                kotlinx.coroutines.delay(delayMs + 1000) // 1s buffer
                refreshRoom()
            }
        } else if (!room.isDeparted) {
            // Already past departure time but flag is false, refresh once
            scope.launch { refreshRoom() }
        }
    }

    override suspend fun refreshRoom() {
        try {
            val updatedRoom = taxiRoomRepository.getRoom(room.id)
            if (room != updatedRoom) {
                room = updatedRoom
                _roomUpdateFlow.emit(updatedRoom)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh room info")
        }
    }

    override fun reconnect() {
        taxiChatService.reconnect()
    }

    override suspend fun fetchInitialChats() {
        if (hasInitialChatsBeenFetched) return
        hasInitialChatsBeenFetched = true
        bind()
        try {
            if (!taxiChatService.isConnectedPublisher.value) {
                taxiChatService.reconnect()
            }
            taxiChatService.isConnectedPublisher.filter { it }.first()
            taxiChatRepository.fetchChats(room.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch initial chats")
        }
    }

    override suspend fun fetchChats(before: Date) {
        try {
            val adjustedDate = Date(before.time - 1)
            withContext(Dispatchers.IO) {
                taxiChatRepository.fetchChatsBefore(room.id, adjustedDate)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch chats")
        }
    }

    override suspend fun sendChat(content: String?, type: TaxiChat.ChatType) {
        // Optimistic insert
        if (content != null) {
            val user = userUseCase.taxiUser
            val tempId = UUID.randomUUID()
            val optimisticChat = TaxiChat(
                id = tempId,
                roomID = room.id,
                type = type,
                authorID = user?.oid,
                authorName = user?.nickname,
                authorProfileURL = user?.profileImageURL,
                authorIsWithdrew = false,
                content = content,
                time = Date(),
                isValid = true,
                inOutNames = null
            )
            synchronized(this) {
                this.flatChats += optimisticChat
            }
            _chats.emit(this.flatChats)

            try {
                val request = TaxiChatRequest(room.id, type, content)
                taxiChatRepository.sendChat(request)
            } catch (e: Exception) {
                synchronized(this) {
                    this.flatChats = this.flatChats.filter { it.id != optimisticChat.id }
                }
                _chats.emit(this.flatChats)
                Timber.tag("TaxiChatUseCase").e(e, "Failed to send chat")
            }
        }
        }

    override suspend fun sendImage(content: Bitmap) {
        val presignedURL = taxiChatRepository.getPresignedURL(room.id)
        val imageData = content.toByteArray()
        taxiChatRepository.uploadImage(presignedURL, imageData)
        taxiChatRepository.notifyImageUploadComplete(presignedURL.id)
    }

    @OptIn(FlowPreview::class)
    private fun bind() {
        synchronized(this) {
            if (isBound) return
            isBound = true
        }

        bindJob?.cancel()
        bindJob = scope.launch {
            // is socket(TaxiChatService) connected
            taxiChatService.isConnectedPublisher
                .onEach { isConnected ->
                    isSocketConnected = isConnected
                    Timber.d("Socket connected: $isConnected")
                    if (isConnected) {
                        refreshRoom() // Sync on reconnect
                    }
                }
                .launchIn(this)

            taxiChatService.chatsPublisher
                .filter { it.isNotEmpty() }
                .distinctUntilChanged { old, new ->
                    old.size == new.size &&
                    old.firstOrNull()?.id == new.firstOrNull()?.id &&
                    old.lastOrNull()?.id == new.lastOrNull()?.id
                }
                .onEach { serverChats ->
                    val latestChatId = serverChats.lastOrNull()?.id

                    synchronized(this@TaxiChatUseCase) {
                        flatChats = serverChats.distinctBy { it.id }
                        accountChats = flatChats.filter { it.type == TaxiChat.ChatType.ACCOUNT }
                    }

                    _chats.value = flatChats

                    // Check if any of the new chats should trigger a room refresh
                    val hasStateChangingChat = serverChats.any { chat ->
                        chat.type == TaxiChat.ChatType.ACCOUNT ||
                        chat.type == TaxiChat.ChatType.SETTLEMENT ||
                        chat.type == TaxiChat.ChatType.PAYMENT
                    }

                    if (hasStateChangingChat) {
                        launch { refreshRoom() }
                    }

                    if (latestChatId != null) {
                        if (!isFirstReadSent || latestChatId != lastReadChatId) {
                            isFirstReadSent = true
                            lastReadChatId = latestChatId
                            launch {
                                try {
                                    taxiChatRepository.readChats(room.id)
                                } catch (e: Exception) {
                                    Timber.e("Read chats failed: ${e.message}")
                                }
                            }
                        }
                    }
                }
                .launchIn(this)

            // handles room updates from chat_update event
            taxiChatService.roomUpdatePublisher
                .filter { it == room.id }
                .debounce(500L)
                .onEach { roomId ->
                    if (roomId != room.id) return@onEach
                    refreshRoom()
                }
                .launchIn(this)
        }
    }

    override fun switchRoom(newRoomId: String) {
        hasInitialChatsBeenFetched = false
        flatChats = emptyList()
        accountChats = emptyList()
        departureRefreshJob?.cancel()

        scope.launch {
            _chats.emit(emptyList())
        }
        taxiChatService.setRoom(newRoomId)
    }
}