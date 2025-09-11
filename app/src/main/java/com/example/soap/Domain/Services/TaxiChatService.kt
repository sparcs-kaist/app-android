package com.example.soap.Domain.Services

import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatDTO
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
    private val tokenStorage: TokenStorageProtocol
) : TaxiChatServiceProtocol {
    private val roomChats = mutableMapOf<String, MutableList<TaxiChat>>()

    private val _chatsFlow = MutableSharedFlow<List<TaxiChat>>(replay = 1)
    override val chatsPublisher = _chatsFlow.asSharedFlow()

    private val _isConnectedFlow = MutableStateFlow(false)
    override val isConnectedPublisher = _isConnectedFlow.asStateFlow()

    private val _roomUpdateFlow = MutableSharedFlow<String>(replay = 1)
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
        set(value) { _isConnectedFlow.value = value }

    private val socket: Socket
    private var currentRoomId: String? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    fun setRoom(roomId: String) {
        currentRoomId?.let { prev ->
            socket.emit("leaveRoom", JSONObject().put("roomId", prev))
        }

        currentRoomId = roomId
        val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }

        serviceScope.launch {
            _chatsFlow.emit(chatsForRoom)
        }

        socket.emit("joinRoom", JSONObject().put("roomId", roomId), Ack {
            socket.emit("request_chat_init", JSONObject().put("roomId", roomId))
        })
    }


    private fun setupSocketEvents() {
        socket.on(Socket.EVENT_CONNECT) {
            isConnected = true
            currentRoomId?.let { roomId ->
                socket.emit("joinRoom", JSONObject().put("roomId", roomId))
                socket.emit("request_chat_init", JSONObject().put("roomId", roomId))
            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            isConnected = false
        }

        socket.on("chat_init") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val chatArrayRaw = firstArg.optJSONArray("chats") ?: return@on
            val newChats = mutableListOf<TaxiChat>()
            for (i in 0 until chatArrayRaw.length()) {
                chatArrayRaw.optJSONObject(i)?.let { json ->
                    parseChatArray(listOf(json.toMap())).let { newChats.addAll(it) }
                }
            }
            chats = newChats.toMutableList()
        }

        socket.on("chat_push_front") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val chatArray = (firstArg.optJSONArray("chats") ?: JSONArray()).let { array ->
                (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.toMap() }
            }
            val newChats = parseChatArray(chatArray)
            chats.addAll(0, newChats)
            serviceScope.launch { _chatsFlow.emit(chats) }
        }

        socket.on("chat_push_back") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val roomId = firstArg.optString("roomId") ?: return@on
            val chatArray = (firstArg.optJSONArray("chats") ?: JSONArray()).let { array ->
                (0 until array.length()).mapNotNull { i -> array.optJSONObject(i)?.toMap() }
            }
            val newChats = parseChatArray(chatArray)
            val chatsForRoom = roomChats.getOrPut(roomId) { mutableListOf() }
            chatsForRoom.addAll(newChats)

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(chatsForRoom) }
            }
        }


        socket.on("chat_update") { args ->
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
}

fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next() as? String ?: continue
        val value = when (val v = this[key]) {
            is JSONArray -> List(v.length()) { i -> v[i] }
            is JSONObject -> v.toMap()
            JSONObject.NULL -> null
            else -> v
        }
        map[key] = value
    }
    return map
}
