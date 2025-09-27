package com.example.soap.Features.TaxiChat

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiParticipant
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Models.Taxi.TaxiUser
import com.example.soap.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import com.example.soap.Domain.Usecases.TaxiChatUseCaseProtocol
import com.example.soap.Domain.Usecases.UserUseCaseProtocol
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaxiChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taxiChatUseCase: TaxiChatUseCaseProtocol,
    private val userUseCase: UserUseCaseProtocol,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol
) : ViewModel(), TaxiChatViewModelProtocol {

    private val initialRoom: TaxiRoom by lazy {
        val json = savedStateHandle.get<String>("room_json")
            ?: throw IllegalStateException("room_json is null. TaxiChatViewModel requires a room_json to initialize.")
        Gson().fromJson(Uri.decode(json), TaxiRoom::class.java)
    }

    sealed class ViewState {
        data object Loading : ViewState()
        data class Loaded(val groupedChats: List<TaxiChatGroup>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    // MARK: - ViewModel Properties
    private val _room = MutableStateFlow(initialRoom)
    override val room: StateFlow<TaxiRoom> = _room.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _groupedChats = MutableStateFlow<List<TaxiChatGroup>>(emptyList())
    override val groupedChats: StateFlow<List<TaxiChatGroup>> = _groupedChats.asStateFlow()

    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser.asStateFlow()

    override var fetchedDateSet: MutableSet<Date> = mutableSetOf()

    private val _isUploading = MutableStateFlow(false)
    override val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private var isFetching = false
    private var isInitialFetching = false

    // MARK: - Setup
    override suspend fun setup() {
        fetchTaxiUser()
        bind()
    }

    init {
        taxiChatUseCase.setRoom(initialRoom)
        bind()

        viewModelScope.launch {
        setup()
            }
    }

    override fun switchRoom(newRoom: TaxiRoom) {
        _room.value = newRoom
        taxiChatUseCase.switchRoom(newRoom.id)
    }

    private fun fetchTaxiUser() {
        _taxiUser.value = userUseCase.taxiUser
    }

    private fun bind() {
        viewModelScope.launch {
            taxiChatUseCase.groupedChatsFlow
                .flowOn(Dispatchers.Main)
                .collect { chats ->
                    _groupedChats.value = chats
                    _state.value = ViewState.Loaded(chats)
                }
        }

        viewModelScope.launch {
            taxiChatUseCase.roomUpdateFlow
                .flowOn(Dispatchers.Main)
                .collect { updatedRoom ->
                    _room.value = updatedRoom
                }
        }
    }

    // MARK: - Chat loading
    override suspend fun fetchChats(before: Date) {
        if (isFetching) return
        isFetching = true
        try {
            taxiChatUseCase.fetchChats(before)
        } finally {
            isFetching = false
        }
    }

    override suspend fun fetchInitialChats() {
        if (isInitialFetching) return
        isInitialFetching = true
        try { taxiChatUseCase.fetchInitialChats() } finally { isInitialFetching = false }
    }

    // MARK: - Chat send
    override suspend fun sendChat(message: String, type: TaxiChat.ChatType) {
        if (type == TaxiChat.ChatType.TEXT && message.isBlank()) return
        viewModelScope.launch {
            taxiChatUseCase.sendChat(message, type)
        }
    }

    // MARK: - Room management
    override suspend fun leaveRoom() {
        taxiRoomRepository.leaveRoom(room.value.id)
    }

    override val isLeaveRoomAvailable: Boolean
        get() = !room.value.isDeparted

    override suspend fun commitSettlement() {
        viewModelScope.launch {
            try {
                val newRoom = taxiRoomRepository.commitSettlement(room.value.id)
                _room.value = newRoom
            } catch (e: Exception) {
                Log.e("TaxiChatViewModel", "Failed to commit settlement for room ${room.value.id}", e)
            }
        }
    }

    override val isCommitSettlementAvailable: Boolean
        get() = room.value.isDeparted && room.value.settlementTotal == 0

    override suspend fun commitPayment() {
        viewModelScope.launch {
            try {
                val newRoom = taxiRoomRepository.commitPayment(room.value.id)
                _room.value = newRoom
            } catch (e: Exception) {
                Log.e("TaxiChatViewModel", "Failed to commit payment for room ${room.value.id}", e)
            }
        }
    }

    override val isCommitPaymentAvailable: Boolean
        get() {
            val me = room.value.participants.firstOrNull { it.id == taxiUser.value?.oid }
            return room.value.isDeparted && room.value.settlementTotal != 0 &&
                    (me?.isSettlement == TaxiParticipant.SettlementType.PaymentRequired)
        }

    override val account: String?
        get() {
            val paidParticipant = room.value.participants.firstOrNull { it.isSettlement == TaxiParticipant.SettlementType.RequestedSettlement }
                ?: return null

            return taxiChatUseCase.accountChats
                .lastOrNull { it.authorID == paidParticipant.id }
                ?.content
        }

    // MARK: - Image upload
    override suspend fun sendImage(image: Bitmap) {

        _isUploading.value = true
        try {
            taxiChatUseCase.sendImage(image)
        } finally {
            _isUploading.value = false
        }
    }
}
