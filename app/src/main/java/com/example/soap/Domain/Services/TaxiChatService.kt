package com.example.soap.Domain.Services

import android.util.Log
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.Domain.Helpers.TokenStorageProtocol
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Networking.ResponseDTO.Taxi.TaxiChatDTO
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


class TaxiChatService  @Inject constructor(
    private val tokenStorage: TokenStorageProtocol
) : TaxiChatServiceProtocol {

    // MARK: - Publisher
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
            Log.d("TaxiChatService", "[TaxiChatService] >>> Connected")
            isConnected = true
        }

        socket.on("chat_init") { args ->
            Log.d("TaxiChatService", "[TaxiChatService] <<< chat_init")
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on

            val chatArrayRaw = firstArg.optJSONArray("chats") ?: return@on

            val chatArray = mutableListOf<Map<String, Any?>>()
            for (i in 0 until chatArrayRaw.length()) {
                val obj = chatArrayRaw.optJSONObject(i) ?: continue
                chatArray.add(obj.toMap())
            }

            val newChats = parseChatArray(chatArray)
            chats.clear()
            chats.addAll(newChats)

            CoroutineScope(Dispatchers.Default).launch {
                _chatsFlow.emit(chats)
            }
        }

        socket.on("chat_push_front") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val dataMap = firstArg.toMap()
            val chatArray =
                (dataMap["chats"] as? List<*>)?.mapNotNull { it as? JSONObject } ?: return@on
            val newChats = parseChatArray(chatArray.map { it.toMap() })
            chats.addAll(0, newChats)
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(chats) }
        }

        socket.on("chat_push_back") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val dataMap = firstArg.toMap()
            val chatArray =
                (dataMap["chats"] as? List<*>)?.mapNotNull { it as? JSONObject } ?: return@on
            val newChats = parseChatArray(chatArray.map { it.toMap() })
            chats.addAll(newChats)
            CoroutineScope(Dispatchers.Default).launch { _chatsFlow.emit(chats) }
        }

        socket.on("chat_update") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val dataMap = firstArg.toMap()
            val roomID = dataMap["roomId"] as? String ?: return@on
            CoroutineScope(Dispatchers.Default).launch { _roomUpdateFlow.emit(roomID) }
        }
    }

    private fun parseChatArray(chatList: List<Map<*, *>>): List<TaxiChat> {
        return chatList.mapNotNull { chatMap ->
            try {
                val dto = Gson().fromJson(Gson().toJson(chatMap), TaxiChatDTO::class.java)
                dto.toModel()
            } catch (e: Exception) {
                Log.e("TaxiChatService", "parseChatArray failed for element: $chatMap", e)
                null
            }
        }
    }
}
fun JSONObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next() as? String ?: continue
        val value = when (val v = this[key]) {
            is JSONArray -> {
                val list = mutableListOf<Any?>()
                for (i in 0 until v.length()) list.add(v[i])
                list
            }
            is JSONObject -> v.toMap()
            JSONObject.NULL -> null
            else -> v
        }
        map[key] = value
    }
    return map
}
