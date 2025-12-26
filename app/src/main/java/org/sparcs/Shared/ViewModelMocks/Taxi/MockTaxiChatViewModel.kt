package org.sparcs.Shared.ViewModelMocks.Taxi

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sparcs.Domain.Models.Taxi.TaxiChat
import org.sparcs.Domain.Models.Taxi.TaxiChatGroup
import org.sparcs.Domain.Models.Taxi.TaxiRoom
import org.sparcs.Domain.Models.Taxi.TaxiUser
import org.sparcs.Features.TaxiChat.TaxiChatViewModel
import org.sparcs.Features.TaxiChat.TaxiChatViewModelProtocol
import org.sparcs.Shared.Mocks.mock
import java.util.Date

class MockTaxiChatViewModel(
    initialState: TaxiChatViewModel.ViewState = TaxiChatViewModel.ViewState.Loading,
    initialGroupedChats: List<TaxiChatGroup> = emptyList(),
    initialTaxiUser: TaxiUser? = null,
    initialUploading: Boolean = false,
    initialRoom: TaxiRoom = TaxiRoom.mock()
) : TaxiChatViewModelProtocol {

    // MARK: - ViewModel Properties

    private val _room = MutableStateFlow(initialRoom)
    override val room: StateFlow<TaxiRoom> = _room.asStateFlow()

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<TaxiChatViewModel.ViewState> = _state

    private val _groupedChats = MutableStateFlow(initialGroupedChats)
    override val groupedChats: StateFlow<List<TaxiChatGroup>> = _groupedChats

    private val _taxiUser = MutableStateFlow(initialTaxiUser)
    override val taxiUser: StateFlow<TaxiUser?> = _taxiUser

    override var fetchedDateSet: MutableSet<Date> = mutableSetOf()

    private val _isUploading = MutableStateFlow(initialUploading)
    override val isUploading: StateFlow<Boolean> = _isUploading

    // MARK: - Computed Properties
    override val isLeaveRoomAvailable: Boolean = true
    override val isCommitSettlementAvailable: Boolean = false
    override val isCommitPaymentAvailable: Boolean = false
    override var account: String? = null

    // MARK: - Functions
    override suspend fun setup() {}
    override suspend fun fetchChats(before: Date) {}
    override suspend fun fetchInitialChats() {}
    override suspend fun sendChat(message: String, type: TaxiChat.ChatType) {}
    override suspend fun leaveRoom() {}
    override suspend fun commitSettlement() {}
    override suspend fun commitPayment() {}
    override suspend fun sendImage(image: Bitmap) {}

    override fun switchRoom(newRoom: TaxiRoom) {}
}
