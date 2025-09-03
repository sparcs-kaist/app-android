package com.example.soap.Domain.Services

import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatDTO
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


class TaxiChatService  @Inject constructor(
    private val tokenStorage: TokenStorageProtocol
) : TaxiChatServiceProtocol {

    // MARK: - Publisher
    private val _chatsFlow = MutableSharedFlow<List<TaxiChat>>(replay = 1)
    override val chatsPublisher = _chatsFlow.asSharedFlow()

    private val _isConnectedFlow = MutableStateFlow(false)
    override val isConnectedPublisher = _isConnectedFlow.asStateFlow()

    private val _roomUpdateFlow = MutableSharedFlow<String>()
    override val roomUpdatePublisher = _roomUpdateFlow.asSharedFlow()

    private var chatsStorage = mutableListOf<TaxiChat>()
    private var chats: MutableList<TaxiChat>
        get() = chatsStorage
        set(value) {
            chatsStorage = value
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(value) }
        }

    private var isConnected: Boolean
        get() = _isConnectedFlow.value
        set(value) {
            _isConnectedFlow.value = value
        }

    // MARK: - Socket
    private val socket: Socket

    init {
        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
            extraHeaders = mutableMapOf(
                "Origin" to listOf("taxi.sparcs.org"),
                "Authorization" to listOf("Bearer ${tokenStorage.getAccessToken() ?: ""}")
            )
        }

        socket = IO.socket(Constants.taxiSocketURL, opts)
        setupSocketEvents()
        socket.connect()
    }

    private fun setupSocketEvents() {
        socket.on(Socket.EVENT_CONNECT) {
            println("[TaxiChatService] >>> Connected")
            isConnected = true
        }

        socket.on("chat_init") { data ->
            val chatArray = parseChatArray(data).toMutableList()
            chats = chatArray
        }

        socket.on("chat_push_front") { data ->
            val newChats = parseChatArray(data)
            chats.addAll(0, newChats)
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(chats) }
        }

        socket.on("chat_push_back") { data ->
            val newChats = parseChatArray(data)
            chats.addAll(newChats)
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(chats) }
        }

        socket.on("chat_update") { data ->
            val roomID = parseRoomId(data) ?: return@on
            CoroutineScope(Dispatchers.Default).launch { _roomUpdateFlow.emit(roomID) }
        }
    }

    private fun parseChatArray(data: Array<Any>): List<TaxiChat> {
        return try {
            val first = data.firstOrNull() ?: return emptyList()
            val jsonString = Json.encodeToString(first)
            val dtoList: List<TaxiChatDTO> = Json.decodeFromString(jsonString)
            dtoList.map { it.toModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseRoomId(data: Array<Any>): String? {
        return try {
            val first = data.firstOrNull() as? Map<*, *> ?: return null
            first["roomId"] as? String
        } catch (e: Exception) {
            null
        }
    }
}