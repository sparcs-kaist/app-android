package org.sparcs.soap.App.Features.TaxiChat

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.sparcs.soap.App.Domain.Helpers.AlertState
import org.sparcs.soap.App.Domain.Models.Taxi.ChatRenderItem
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiChat
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiParticipant
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiRoom
import org.sparcs.soap.App.Domain.Models.Taxi.TaxiUser
import org.sparcs.soap.App.Domain.Repositories.Taxi.TaxiRoomRepositoryProtocol
import org.sparcs.soap.App.Domain.Usecases.Taxi.TaxiChatUseCaseProtocol
import org.sparcs.soap.App.Domain.Usecases.UserUseCaseProtocol
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatBubblePositionResolver
import org.sparcs.soap.App.Features.TaxiChat.Components.ChatRenderItemBuilder
import org.sparcs.soap.App.Features.TaxiChat.Components.DefaultMessagePresentationPolicy
import org.sparcs.soap.App.Features.TaxiChat.Components.TaxiGroupingPolicy
import org.sparcs.soap.App.Shared.Extensions.toAlertState
import org.sparcs.soap.R
import timber.log.Timber
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


interface TaxiChatViewModelProtocol {

    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiChatViewModel.ViewState>
    val renderItems: StateFlow<List<ChatRenderItem>>
    val taxiUser: StateFlow<TaxiUser?>

    var alertState: AlertState?
    var isAlertPresented: Boolean

    val room: StateFlow<TaxiRoom>
    val isUploading: StateFlow<Boolean>

    // MARK: - Computed Properties
    val isLeaveRoomAvailable: Boolean
    val isCommitSettlementAvailable: Boolean
    val isCommitPaymentAvailable: Boolean
    val account: String?
    val topChatID: String?

    var scrollToBottomTrigger: Int

    // MARK: - Functions
    suspend fun setup()

    suspend fun loadMoreChats()
    suspend fun fetchInitialChats()
    suspend fun sendChat(message: String, type: TaxiChat.ChatType)
    suspend fun leaveRoom()
    suspend fun commitSettlement()
    suspend fun commitPayment()
    suspend fun sendImage(image: Bitmap)
    fun switchRoom(newRoom: TaxiRoom)
    suspend fun toggleCarrier(hasCarrier: Boolean)
    suspend fun updateArrival(isArrived: Boolean)
}

@HiltViewModel
class TaxiChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val taxiChatUseCase: TaxiChatUseCaseProtocol,
    val userUseCase: UserUseCaseProtocol,
    private val taxiRoomRepository: TaxiRoomRepositoryProtocol,
) : ViewModel(), TaxiChatViewModelProtocol {

    private val initialRoom: TaxiRoom by lazy {
        val json = savedStateHandle.get<String>("room_json")
            ?: throw IllegalStateException("room_json is null. TaxiChatViewModel requires a room_json to initialize.")
        Gson().fromJson(Uri.decode(json), TaxiRoom::class.java)
    }

    sealed class ViewState {
        data object Loading : ViewState()
        data object Loaded : ViewState()
        data class Error(val error: Exception) : ViewState()
    }

    // MARK: - ViewModel Properties
    private val _room = MutableStateFlow(initialRoom)
    override val room: StateFlow<TaxiRoom> = _room.asStateFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    override val state: StateFlow<ViewState> = _state.asStateFlow()

    private val _taxiUser = MutableStateFlow<TaxiUser?>(null)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser.asStateFlow()

    override var topChatID: String? = null
        private set

    private var fetchedDateSet: MutableSet<Date> = mutableSetOf()

    override var alertState: AlertState? by mutableStateOf(null)
    override var isAlertPresented: Boolean by mutableStateOf(false)

    private val _isUploading = MutableStateFlow(false)
    override val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private var isFetching = false
    private var isInitialFetching = false
    override var scrollToBottomTrigger = 0
    private val isBound = AtomicBoolean(false)

    private val builder = ChatRenderItemBuilder(
        policy = TaxiGroupingPolicy(),
        positionResolver = ChatBubblePositionResolver(),
        presentationPolicy = DefaultMessagePresentationPolicy()
    )
    private val _renderItems = MutableStateFlow<List<ChatRenderItem>>(emptyList())
    override val renderItems: StateFlow<List<ChatRenderItem>> = _renderItems.asStateFlow()

    // MARK: - Setup
    override suspend fun setup() {
        fetchTaxiUser()
        taxiChatUseCase.setRoom(room.value)
        bind()
    }

    override fun switchRoom(newRoom: TaxiRoom) {
        _room.value = newRoom
        taxiChatUseCase.switchRoom(newRoom.id)
    }

    private suspend fun fetchTaxiUser() {
        if (userUseCase.taxiUser == null) {
            try {
                userUseCase.fetchTaxiUser()
            } catch (e: Exception) {
                Timber.tag("TaxiChatViewModel").e(e, "Failed to fetch taxi user")
            }
        }
        _taxiUser.value = userUseCase.taxiUser
    }

    private fun bind() {
        if (!isBound.compareAndSet(false, true)) return
        taxiChatUseCase.chats
            .onEach { chats ->
                val distinctChats = chats.distinctBy { it.id }
                val myId = userUseCase.taxiUser?.oid ?: ""
                val filtered = distinctChats.filter { it.roomID == room.value.id }

                val builtItems = builder.build(filtered, myUserID = myId)

                if (_renderItems.value == builtItems) {
                    return@onEach
                }

                val lastItemId = _renderItems.value.lastOrNull()?.id
                val newLastItemId = builtItems.lastOrNull()?.id

                _renderItems.value = builtItems

                if (lastItemId != null && lastItemId != newLastItemId) {
                    scrollToBottomTrigger += 1
                }

                if (builtItems.isNotEmpty() && _state.value is ViewState.Loading) {
                    _state.value = ViewState.Loaded
                }
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)


        taxiChatUseCase.roomUpdateFlow
            .onEach { updatedRoom ->
                _room.value = updatedRoom
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    // MARK: - Chat loading
    override suspend fun loadMoreChats() {
//        if (isFetching) return
//
//        val oldestChatTime = renderItems.value.firstOrNull()? ?: return
//
//        if (fetchedDateSet.contains(oldestChatTime)) return
//
//        isFetching = true
//        try {
//            taxiChatUseCase.fetchChats(before = oldestChatTime)
//            fetchedDateSet.add(oldestChatTime)
//        } catch (e: Exception) {
//            Timber.e(e, "loadMoreChats failed")
//        } finally {
//            isFetching = false
//        }
    }

    override suspend fun fetchInitialChats() {
        if (isInitialFetching) return
        isInitialFetching = true
        try {
            taxiChatUseCase.fetchInitialChats()
            _state.value = ViewState.Loaded
        } finally {
            isInitialFetching = false
        }
    }

    // MARK: - Chat send
    override suspend fun sendChat(message: String, type: TaxiChat.ChatType) {
        if (type == TaxiChat.ChatType.TEXT && message.isBlank()) return
        taxiChatUseCase.sendChat(message, type)

        scrollToBottomTrigger += 1
    }

    // MARK: - Room management
    override suspend fun leaveRoom() {
        taxiRoomRepository.leaveRoom(room.value.id)
    }

    override val isLeaveRoomAvailable: Boolean
        get() = !room.value.isDeparted

    override val isCommitSettlementAvailable: Boolean
        get() = room.value.isDeparted && room.value.settlementTotal == 0


    override suspend fun commitSettlement() {
        try {
            val newRoom = taxiRoomRepository.commitSettlement(room.value.id)
            _room.value = newRoom

            val me = newRoom.participants.firstOrNull { it.id == taxiUser.value?.oid }
            if (me?.isSettlement == TaxiParticipant.SettlementType.RequestedSettlement) {
                val myAccount = _taxiUser.value?.account
                taxiChatUseCase.sendChat(myAccount, TaxiChat.ChatType.ACCOUNT)
            }
        } catch (e: Exception) {
            this.alertState = e.toAlertState(R.string.error_settlement_failed)
            this.isAlertPresented = true

            Timber.e(e, "commitSettlement failed")
        }
    }


    override suspend fun commitPayment() {
        try {
            val newRoom = taxiRoomRepository.commitPayment(room.value.id)
            _room.value = newRoom
        } catch (e: Exception) {
            this.alertState = e.toAlertState(R.string.error_payment_failed)
            this.isAlertPresented = true
            Timber.e(e, "Failed to commit payment")
        }
    }

    override suspend fun toggleCarrier(hasCarrier: Boolean) {
        try {
            val updatedRoom = taxiRoomRepository.toggleCarrier(
                id = room.value.id,
                hasCarrier = hasCarrier
            )

            _room.value = updatedRoom

            taxiChatUseCase.setRoom(updatedRoom)

        } catch (e: Exception) {
            this.alertState = e.toAlertState(R.string.error_toggle_carrier_failed)
            this.isAlertPresented = true
            Timber.e(e, "toggleCarrier failed")
        }
    }

    override suspend fun updateArrival(isArrived: Boolean) {
        try {
            val updatedRoom = taxiRoomRepository.updateArrival(
                id = room.value.id,
                isArrived = isArrived
            )

            _room.value = updatedRoom

            taxiChatUseCase.setRoom(updatedRoom)

        } catch (e: Exception) {
            this.alertState = e.toAlertState(R.string.error_update_arrival_failed)
            this.isAlertPresented = true
            Timber.e(e, "updateArrival failed")
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
            val paidParticipant = room.value.participants
                .firstOrNull { it.isSettlement == TaxiParticipant.SettlementType.RequestedSettlement }
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

            scrollToBottomTrigger += 1
        } finally {
            _isUploading.value = false
        }
    }
}
