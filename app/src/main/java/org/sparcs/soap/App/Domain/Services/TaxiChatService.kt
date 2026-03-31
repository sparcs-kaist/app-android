package org.sparcs.soap.App.Domain.Services

import com.google.gson.Gson
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Domain.Helpers.TokenStorageProtocol
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Domain.Usecases.AuthUseCaseProtocol
import org.sparcs.soap.App.Networking.ResponseDTO.Taxi.TaxiChatDTO
import org.sparcs.soap.App.Shared.Extensions.toMap
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

interface TaxiChatServiceProtocol {
    val chatsPublisher: Flow<List<TaxiChat>>
    val isConnectedPublisher: Flow<Boolean>
    val roomUpdatePublisher: Flow<String>
    fun reconnect()
    fun disconnect()
}

class TaxiChatService @Inject constructor(
    private val tokenStorage: TokenStorageProtocol,
    private val authUseCaseProvider: Provider<AuthUseCaseProtocol>,
) : TaxiChatServiceProtocol {
    private val authUseCase get() = authUseCaseProvider.get()

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
        set(value) {
            _isConnectedFlow.value = value
        }

    // MARK: - State
    private var hasAttemptedReconnect: Boolean = false

    private var socket: Socket? = null
    private var currentRoomId: String? = null

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        serviceScope.launch {
            authUseCase.isAuthenticatedFlow.collect { isAuth ->
                if (!isAuth) {
                    disconnect()
                } else {
                    reconnect()
                }
            }
        }
    }

    private fun reconnectSocketWithToken(token: String?) {
        socket?.off()
        socket?.disconnect()
        socket = null

        if (token == null) {
            Timber.e("Token is null, cannot reconnect.")
            return
        }

        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 2000
            extraHeaders = mutableMapOf(
                "Origin" to listOf("taxi.sparcs.org"),
                "Authorization" to listOf("Bearer $token")
            )
        }

        try {
            socket = IO.socket(Constants.taxiSocketURL, opts)
            setupSocketEvents()
            socket?.connect()
        } catch (e: Exception) {
            Timber.e("Socket creation failed: ${e.message}")
        }
    }


    fun setRoom(roomId: String) {
        serviceScope.launch {
            _chatsFlow.emit(emptyList())

            currentRoomId?.let { prev ->
                socket?.emit("leaveRoom", JSONObject().put("roomId", prev))
            }

            currentRoomId = roomId

            roomChats[roomId] = mutableListOf()

            if (socket?.connected() == true) {
                socket?.emit("joinRoom", JSONObject().put("roomId", roomId), Ack {
                    socket?.emit("request_chat_init", JSONObject().put("roomId", roomId))
                })
            }
        }
    }

    private fun setupSocketEvents() {
        socket?.on(Socket.EVENT_CONNECT) {
            isConnected = true
            this.hasAttemptedReconnect = false
            currentRoomId?.let { roomId ->
                socket?.emit("joinRoom", JSONObject().put("roomId", roomId))
                socket?.emit("request_chat_init", JSONObject().put("roomId", roomId))
            }
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            isConnected = false

            if (!hasAttemptedReconnect) {
                Timber.d("[TaxiChatService] Socket disconnected (hasAttemptedReconnect=false)")
            }
        }


        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Timber.e("[TaxiChatService] Socket error: ${args.getOrNull(0)}")
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

            val uniqueChats = newChats.distinctBy { it.id }.toMutableList()
            roomChats[roomId] = uniqueChats

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(uniqueChats.toList()) }
            }
        }

        socket?.on("chat_push_front") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomId = firstArg.optString("roomId", null) ?: currentRoomId ?: return@on
            val chatArrayRaw = firstArg.optJSONArray("chats") ?: return@on

            val newChats = (0 until chatArrayRaw.length()).mapNotNull { i ->
                chatArrayRaw.optJSONObject(i)?.let { parseChatObject(it.toMap()) }
            }

            val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }
            val uniqueNewChats =
                newChats.filter { newChat -> chatsForRoom.none { it.id == newChat.id } }
            chatsForRoom.addAll(0, uniqueNewChats)

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(chatsForRoom.toList()) }
            }

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
            val filteredChats = newChats.filter { !existingIds.contains(it.id) }

            chatsForRoom.addAll(filteredChats)

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(chatsForRoom.toList()) }
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
            try {
                Gson().fromJson(Gson().toJson(it), TaxiChatDTO::class.java).toModel()
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseChatObject(chatMap: Map<*, *>): TaxiChat? {
        return try {
            Gson().fromJson(Gson().toJson(chatMap), TaxiChatDTO::class.java).toModel()
        } catch (e: Exception) {
            null
        }
    }

    override fun disconnect() {
        socket?.off()
        socket?.disconnect()
        socket = null
        _isConnectedFlow.value = false
    }

    override fun reconnect() {
        Timber.d("[TaxiChatService] Reconnecting socket...")
        val token = tokenStorage.getAccessToken()
        reconnectSocketWithToken(token)
    }
}