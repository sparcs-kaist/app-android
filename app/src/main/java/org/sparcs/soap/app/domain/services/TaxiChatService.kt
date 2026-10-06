package org.sparcs.soap.app.domain.services

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.sparcs.soap.app.domain.helpers.Constants
import org.sparcs.soap.app.domain.helpers.TokenStorageProtocol
import org.sparcs.soap.app.domain.models.taxi.TaxiChat
import org.sparcs.soap.app.domain.usecases.AuthUseCaseProtocol
import org.sparcs.soap.app.networking.responseDTO.taxi.TaxiChatDTO
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

interface TaxiChatServiceProtocol {
    val chatsPublisher: Flow<List<TaxiChat>>
    val isConnectedPublisher: Flow<Boolean>
    val roomUpdatePublisher: Flow<String>
    fun connectIfNeeded()
    fun reconnect()
    fun disconnect()
}

class MockTaxiChatService : TaxiChatServiceProtocol {
    override val chatsPublisher: Flow<List<TaxiChat>> = MutableStateFlow(emptyList())
    override val isConnectedPublisher: Flow<Boolean> = MutableStateFlow(false)
    override val roomUpdatePublisher: Flow<String> = MutableSharedFlow()
    override fun connectIfNeeded() {}
    override fun reconnect() {}
    override fun disconnect() {}
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
    private val gson = Gson()

    private var isConnected: Boolean
        get() = _isConnectedFlow.value
        set(value) {
            _isConnectedFlow.value = value
        }

    private var hasAttemptedTokenRefresh: Boolean = false
    private var reconnectJob: Job? = null

    private var socket: Socket? = null
    private var currentRoomId: String? = null

    init {
        observeAuthState()
        observeForeground()
    }

    private fun observeForeground() {
        serviceScope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (socket != null && !isConnected) reconnect()
                }
            })
        }
    }

    private fun observeAuthState() {
        serviceScope.launch {
            authUseCase.isAuthenticatedFlow.collect { isAuth ->
                if (!isAuth) {
                    disconnect()
                } else {
                    connectIfNeeded()
                }
            }
        }
    }

    private fun connectSocket() {
        closeSocket()

        val opts = IO.Options().apply {
            forceNew = true
            reconnection = true
            reconnectionDelay = 2000
            reconnectionDelayMax = 30000
            randomizationFactor = 0.5
        }

        try {
            socket = IO.socket(Constants.TAXI_SOCKET_URL, opts).also { newSocket ->
                newSocket.io().on(Manager.EVENT_TRANSPORT) { args ->
                    (args.firstOrNull() as? Transport)?.on(Transport.EVENT_REQUEST_HEADERS) { headerArgs ->
                        @Suppress("UNCHECKED_CAST")
                        val headers = headerArgs.firstOrNull() as? MutableMap<String, List<String>> ?: return@on
                        headers["Origin"] = listOf("taxi.sparcs.org")
                        tokenStorage.getAccessToken()?.let { headers["Authorization"] = listOf("Bearer $it") }
                    }
                }
            }
            setupSocketEvents()
            socket?.connect()
        } catch (e: Exception) {
            Timber.e("Socket creation failed: ${e.message}")
        }
    }

    private fun closeSocket() {
        socket?.io()?.off()
        socket?.off()
        socket?.disconnect()
        socket = null
    }


    fun setRoom(roomId: String) {
        currentRoomId = roomId
        roomChats[roomId] = mutableListOf()
        serviceScope.launch { _chatsFlow.emit(emptyList()) }
    }

    private fun setupSocketEvents() {
        socket?.on(Socket.EVENT_CONNECT) {
            isConnected = true
            hasAttemptedTokenRefresh = false
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            isConnected = false
        }


        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Timber.e("[TaxiChatService] Socket error: ${args.getOrNull(0)}")
            if (!hasAttemptedTokenRefresh) {
                hasAttemptedTokenRefresh = true
                reconnect(forceRefresh = true)
            }
        }

        socket?.on("chat_init") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val newChats = parseChats(firstArg.optJSONArray("chats") ?: return@on)
            val roomId = roomIdOf(firstArg, newChats) ?: return@on

            val uniqueChats = newChats.distinctBy { it.id }.toMutableList()
            roomChats[roomId] = uniqueChats

            if (roomId == currentRoomId) {
                serviceScope.launch { _chatsFlow.emit(uniqueChats.toList()) }
            }
        }

        socket?.on("chat_push_front") { args ->
            val firstArg = args.firstOrNull() as? JSONObject ?: return@on
            val newChats = parseChats(firstArg.optJSONArray("chats") ?: return@on)
            val roomId = roomIdOf(firstArg, newChats) ?: return@on

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
            val newChats = parseChats(firstArg.optJSONArray("chats") ?: JSONArray())
            val roomId = roomIdOf(firstArg, newChats) ?: return@on
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

    private fun parseChats(array: JSONArray): List<TaxiChat> =
        (0 until array.length()).mapNotNull { i ->
            val json = array.optJSONObject(i) ?: return@mapNotNull null
            try {
                gson.fromJson(json.toString(), TaxiChatDTO::class.java).toModel()
            } catch (_: Exception) {
                null
            }
        }

    private fun roomIdOf(payload: JSONObject, chats: List<TaxiChat>): String? =
        payload.optString("roomId").ifEmpty { null }
            ?: chats.firstOrNull()?.roomID
            ?: currentRoomId

    override fun connectIfNeeded() {
        if (reconnectJob?.isActive == true || socket?.isActive == true) return
        reconnect()
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        closeSocket()
        hasAttemptedTokenRefresh = false
        _isConnectedFlow.value = false
    }

    override fun reconnect() = reconnect(forceRefresh = false)

    private fun reconnect(forceRefresh: Boolean) {
        Timber.d("[TaxiChatService] Reconnecting socket...")
        reconnectJob?.cancel()
        reconnectJob = serviceScope.launch {
            try {
                if (forceRefresh) authUseCase.refreshAccessToken(force = true) else authUseCase.getValidAccessToken()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "[TaxiChatService] Unable to obtain a valid token")
                if (tokenStorage.getAccessToken() == null) return@launch
            }
            connectSocket()
        }
    }
}