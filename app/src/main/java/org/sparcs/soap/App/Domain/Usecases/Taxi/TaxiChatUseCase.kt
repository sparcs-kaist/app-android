package org.sparcs.soap.App.Domain.Usecases.Taxi

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChatRequest
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiChatRepositoryProtocol
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.App.Domain.Services.TaxiChatService
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Shared.Extensions.toByteArray
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

interface TaxiChatUseCaseProtocol {
    val chats: SharedFlow<List<TaxiChat>>
    val roomUpdateFlow: Flow<TaxiRoom>
    val accountChats: List<TaxiChat>

    fun setRoom(room: TaxiRoom)
    fun reconnect()
    suspend fun fetchInitialChats()
    suspend fun fetchChats(before: Date)
    suspend fun sendChat(content: String?, type: TaxiChat.ChatType)
    suspend fun sendImage(content: Bitmap)
    fun switchRoom(newRoomId: String)
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
    private val _chats = MutableSharedFlow<List<TaxiChat>>()
    override val chats: SharedFlow<List<TaxiChat>> = _chats.asSharedFlow()

    private val _roomUpdateFlow = MutableSharedFlow<TaxiRoom>()
    override val roomUpdateFlow: Flow<TaxiRoom> = _roomUpdateFlow.asSharedFlow()

    private var isSocketConnected: Boolean = false
    private var hasInitialChatsBeenFetched: Boolean = false
    private var flatChats: List<TaxiChat> = emptyList()

    // MARK: - Computed Properties
    override var accountChats: List<TaxiChat> = emptyList()

    private var isBound = false

    override fun setRoom(room: TaxiRoom) {
        this.room = room
        this.flatChats = emptyList()
        this.hasInitialChatsBeenFetched = false
        taxiChatService.setRoom(room.id)
    }

    override fun reconnect() {
        taxiChatService.reconnect()
    }

    override suspend fun fetchInitialChats() {
        if (hasInitialChatsBeenFetched) return
        hasInitialChatsBeenFetched = true
        bind()
        try {
            taxiChatService.isConnectedPublisher.filter { it }.first()
            taxiChatRepository.fetchChats(room.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch initial chats")
        }
    }

    override suspend fun fetchChats(before: Date) {
        scope.launch(Dispatchers.IO) {
            try {
                taxiChatRepository.fetchChatsBefore(room.id, before)
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch chats")
            }
        }
    }

    override suspend fun sendChat(content: String?, type: TaxiChat.ChatType) {
        // Optimistic insert
        if (content != null) {
            val user = userUseCase.taxiUser
            val optimisticChat = TaxiChat(
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
            flatChats = flatChats + optimisticChat
            _chats.emit(flatChats)
        }

        try {
            val request = TaxiChatRequest(room.id, type, content)
            taxiChatRepository.sendChat(request)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send chat")
        }
    }

    override suspend fun sendImage(content: Bitmap) {
        val presignedURL = taxiChatRepository.getPresignedURL(room.id)
        val imageData = content.toByteArray()
        taxiChatRepository.uploadImage(presignedURL, imageData)
        taxiChatRepository.notifyImageUploadComplete(presignedURL.id)
    }

    private fun bind() {
        if (isBound) return
        isBound = true
        // is socket(TaxiChatService) connected
        taxiChatService.isConnectedPublisher
            .onEach { isConnected ->
                isSocketConnected = isConnected
                Timber.d("Socket connected: $isConnected")
            }
            .launchIn(scope)

        taxiChatService.chatsPublisher
            .onEach { newChats ->
                try {
                    taxiChatRepository.readChats(room.id)
                } catch (e: Exception) {
                }

                this.flatChats = newChats
                this.accountChats = newChats.filter { it.type == TaxiChat.ChatType.ACCOUNT }

                _chats.emit(newChats)
            }
            .launchIn(scope)

        // handles room updates from chat_update event
        taxiChatService.roomUpdatePublisher
            .onEach { roomId ->
                if (roomId != room.id) return@onEach
                try {
                    val updatedRoom = taxiRoomRepository.getRoom(roomId)
                    this.room = updatedRoom
                    _roomUpdateFlow.emit(updatedRoom)
                } catch (e: Exception) {
                    Timber.e("Failed to update room: $e")
                }
            }
            .launchIn(scope)
    }

    override fun switchRoom(newRoomId: String) {
        hasInitialChatsBeenFetched = false
        flatChats = emptyList()
        accountChats = emptyList()

        scope.launch {
            _chats.emit(emptyList())
        }
        taxiChatService.setRoom(newRoomId)
    }
}