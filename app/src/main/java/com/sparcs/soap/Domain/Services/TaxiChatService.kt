package com.sparcs.soap.Domain.Services

import com.sparcs.soap.Domain.Helpers.Constants
import com.sparcs.soap.Domain.Helpers.TokenStorageProtocol
import com.sparcs.soap.Domain.Models.Taxi.TaxiChat
import com.sparcs.soap.Domain.Usecases.AuthUseCaseProtocol
import com.sparcs.soap.Networking.ResponseDTO.Taxi.TaxiChatDTO
import com.sparcs.soap.Shared.Extensions.toMap
import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class TaxiChatService @Inject constructor(
    private val tokenStorage: TokenStorageProtocol,
    private val authUseCase: AuthUseCaseProtocol
) : TaxiChatServiceProtocol {
    private val roomChats = mutableMapOf<String, MutableList<TaxiChat>>()

    private val _chatsFlow = MutableSharedFlow<List<TaxiChat>>(replay = 1)
    override val chatsPublisher = _chatsFlow.asSharedFlow()

    private val _isConnectedFlow = MutableStateFlow(false)
    override val isConnectedPublisher = _isConnectedFlow.asStateFlow()

    private val _roomUpdateFlow = MutableSharedFlow<String>(replay = 1)
    override val roomUpdatePublisher = _roomUpdateFlow.asSharedFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var chatsStorage = mutableListOf<TaxiChat>()
    private var chats: MutableList<TaxiChat>
        get() = chatsStorage
        set(value) {
            chatsStorage = value
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(value) }
        }

    private var isConnected: Boolean
        get() = _isConnectedFlow.value
        set(value) { _isConnectedFlow.value = value }

    private var socket: Socket? = null
    private var currentRoomId: String? = null

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        serviceScope.launch {
            authUseCase.isAuthenticatedFlow.collect { isAuth ->
                if (!isAuth) {
                    socket?.disconnect()
                    _isConnectedFlow.value = false
                } else {
                    reconnectSocketWithToken(tokenStorage.getAccessToken())
                }
            }
        }
    }

    private fun reconnectSocketWithToken(token: String?) {
        if (token == null) return

        try { socket?.disconnect() } catch (_: Exception) {}

        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
            extraHeaders = mutableMapOf(
                "Origin" to listOf("taxi.sparcs.org"),
                "Authorization" to listOf("Bearer $token")
            )
        }

        socket = IO.socket(Constants.taxiSocketURL, opts)
        setupSocketEvents()
        socket?.connect()
    }


    fun setRoom(roomId: String) {
        currentRoomId?.let { prev ->
            socket?.emit("leaveRoom", JSONObject().put("roomId", prev))
        }

        currentRoomId = roomId

        val chatsForRoom = mutableListOf<TaxiChat>()
        roomChats[roomId] = chatsForRoom

        serviceScope.launch {
            _chatsFlow.emit(chatsForRoom)
        }

        socket?.emit("joinRoom", JSONObject().put("roomId", roomId), Ack {
            socket?.emit("request_chat_init", JSONObject().put("roomId", roomId))
        })
    }

    private fun setupSocketEvents() {
        socket?.on(Socket.EVENT_CONNECT) {
            isConnected = true
            currentRoomId?.let { roomId ->
                socket?.emit("joinRoom", JSONObject().put("roomId", roomId))
                socket?.emit("request_chat_init", JSONObject().put("roomId", roomId))
            }
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            isConnected = false
        }

        // chat_init
        socket?.on("chat_init") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomId = firstArg.optString("roomId", null) ?: currentRoomId ?: return@on
            val chatArrayRaw = firstArg.optJSONArray("chats") ?: return@on

            val newChats = mutableListOf<TaxiChat>()
            for (i in 0 until chatArrayRaw.length()) {
                chatArrayRaw.optJSONObject(i)?.let { json ->
                    parseChatObject(json.toMap())?.let { newChats.add(it) }
                }
            }

            val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }.apply {
                clear()
                addAll(newChats)
            }

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(chatsForRoom) }
            }
        }

        socket?.on("chat_push_front") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomId = firstArg.optString("roomId", null) ?: currentRoomId ?: return@on
            val chatArray = (firstArg.optJSONArray("chats") ?: JSONArray()).let { array ->
                (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.toMap() }
            }
            val newChats = parseChatArray(chatArray)
            val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }

            chatsForRoom.addAll(0, newChats)
            serviceScope.launch { _chatsFlow.emit(chatsForRoom) }
        }

        socket?.on("chat_push_back") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomId = firstArg.optString("roomId") ?: return@on
            val chatArray = (firstArg.optJSONArray("chats") ?: JSONArray()).let { array ->
                (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.toMap() }
            }
            val newChats = parseChatArray(chatArray)
            val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }

            val existingIds = chatsForRoom.map { it.id }.toSet()
            val filteredChats = newChats.filter { newChat ->
                !existingIds.contains(newChat.id)
            }

            chatsForRoom.addAll(filteredChats)

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(chatsForRoom) }
            }
        }

        socket?.on("chat_update") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomID = firstArg.optString("roomId") ?: return@on
            serviceScope.launch { _roomUpdateFlow.emit(roomID) }
        }
    }

    private fun parseChatArray(chatList: List<Map<*, *>>): List<TaxiChat> {
        return chatList.mapNotNull {
            try { Gson().fromJson(Gson().toJson(it), TaxiChatDTO::class.java).toModel() }
            catch (e: Exception) { null }
        }
    }
    private fun parseChatObject(chatMap: Map<*, *>): TaxiChat? {
        return try {
            Gson().fromJson(Gson().toJson(chatMap), TaxiChatDTO::class.java).toModel()
        } catch (e: Exception) { null }
    }
}