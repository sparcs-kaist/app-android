package com.example.soap.Domain.Usecases

import android.graphics.Bitmap
import android.util.Log
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiChatRequest
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Repositories.TaxiChatRepository
import com.example.soap.Domain.Repositories.TaxiRoomRepository
import com.example.soap.Domain.Services.TaxiChatService
import com.example.soap.Shared.Extensions.toByteArray
import com.example.soap.Shared.Extensions.toISO8601
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class TaxiChatUseCase @Inject constructor(
    private val taxiChatService: TaxiChatService,
    private val userUseCase: UserUseCase,
    private val taxiChatRepository: TaxiChatRepository,
    private val taxiRoomRepository: TaxiRoomRepository
): TaxiChatUseCaseProtocol {

    private lateinit var room: TaxiRoom

    override fun setRoom(room: TaxiRoom) {
        this.room = room
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // MARK: - Flows
    private val _groupedChatsFlow = MutableStateFlow<List<TaxiChatGroup>>(emptyList())
    override val groupedChatsFlow: Flow<List<TaxiChatGroup>> = _groupedChatsFlow.asStateFlow()

    private val _roomUpdateFlow = MutableSharedFlow<TaxiRoom>()
    override val roomUpdateFlow: Flow<TaxiRoom> = _roomUpdateFlow.asSharedFlow()

    private var isSocketConnected: Boolean = false
    private var hasInitialChatsBeenFetched: Boolean = false

    init {
        bind()
    }

    override suspend fun fetchInitialChats() {
        if (!::room.isInitialized) return
        if (hasInitialChatsBeenFetched) return
        hasInitialChatsBeenFetched = true
        try {
            taxiChatRepository.fetchChats(room.id)
        } catch (e: Exception) {
            Log.e("TaxiChatUseCase", "Failed to fetch initial chats", e)
        }
    }

    override suspend fun fetchChats(before: Date) {
        scope.launch(Dispatchers.IO) {
            try {
                taxiChatRepository.fetchChatsBefore(room.id, before)
            } catch (e: Exception) {
                Log.e("TaxiChatUseCase", "Failed to fetch chats", e)
            }
        }
    }

    override suspend fun sendChat(content: String?, type: TaxiChat.ChatType) {
        scope.launch(Dispatchers.IO) {
            try {
                val request = TaxiChatRequest(room.id, type, content)
                taxiChatRepository.sendChat(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun sendImage(content: Bitmap) {
        val presignedURL = taxiChatRepository.getPresignedURL(room.id)
        val imageData = content.toByteArray()
        taxiChatRepository.uploadImage(presignedURL, imageData)
        taxiChatRepository.notifyImageUploadComplete(presignedURL.id)
    }

    private fun bind() {
        // is socket(TaxiChatService) connected
        if (!::room.isInitialized) return
        scope.launch(Dispatchers.Default) {
            taxiChatService.isConnectedPublisher.collect { isConnected ->
                isSocketConnected = isConnected
            }
        }

        // converts [TaxiChat] into [TaxiChatGroup]
        scope.launch(Dispatchers.Default) {
            taxiChatService.chatsPublisher.collect { chats ->
                taxiChatRepository.readChats(room.id)

                val user = userUseCase.taxiUser
                val grouped = groupChats(chats, user?.oid ?: "")

                _groupedChatsFlow.value = grouped
            }
        }

        // handles room updates from chat_update event
        scope.launch(Dispatchers.Default) {
            taxiChatService.roomUpdatePublisher.collect { roomId ->
                if (roomId != room.id) return@collect
                try {
                    val updatedRoom = taxiRoomRepository.getRoom(roomId)
                    room = updatedRoom
                    _roomUpdateFlow.emit(updatedRoom)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun groupChats(chats: List<TaxiChat>, currentUserID: String): List<TaxiChatGroup> {
        if (chats.isEmpty()) return emptyList()

        val result = mutableListOf<TaxiChatGroup>()
        val currentGroup = mutableListOf<TaxiChat>()

        fun flushGroup() {
            if (currentGroup.isNotEmpty()) {
                val first = currentGroup.first()
                val group = TaxiChatGroup(
                    id = first.time.toISO8601(),
                    chats = currentGroup.toList(),
                    lastChatID = currentGroup.lastOrNull()?.id,
                    authorID = first.authorID,
                    authorName = first.authorName,
                    authorProfileURL = first.authorProfileURL,
                    authorIsWithdrew = first.authorIsWithdrew,
                    time = first.time,
                    isMe = first.authorID == currentUserID,
                    isGeneral = false
                )
                result.add(group)
                currentGroup.clear()
            }
        }

        val calendar = Calendar.getInstance()

        for (chat in chats) {
            if (chat.type == TaxiChat.ChatType.ENTRANCE || chat.type == TaxiChat.ChatType.EXIT) {
                flushGroup()
                result.add(
                    TaxiChatGroup(
                        id = chat.time.toISO8601(),
                        chats = listOf(chat),
                        lastChatID = null,
                        authorID = chat.authorID,
                        authorName = chat.authorName,
                        authorProfileURL = chat.authorProfileURL,
                        authorIsWithdrew = chat.authorIsWithdrew,
                        time = chat.time,
                        isMe = chat.authorID == currentUserID,
                        isGeneral = true
                    )
                )
                continue
            }

            if (currentGroup.isEmpty()) {
                currentGroup.add(chat)
                continue
            }

            val lastChat = currentGroup.last()
            val isSameAuthor = chat.authorID == lastChat.authorID
            val isSameMinute = calendar.apply { time = chat.time }.get(Calendar.MINUTE) ==
                    calendar.apply { time = lastChat.time }.get(Calendar.MINUTE)

            if (isSameAuthor && isSameMinute) {
                currentGroup.add(chat)
            } else {
                flushGroup()
                currentGroup.add(chat)
            }
        }

        flushGroup()
        return result
    }
}