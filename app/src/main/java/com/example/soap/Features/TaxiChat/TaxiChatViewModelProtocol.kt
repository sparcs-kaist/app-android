package com.example.soap.Features.TaxiChat

import android.graphics.Bitmap
import com.example.soap.Domain.Models.Taxi.TaxiChat
import com.example.soap.Domain.Models.Taxi.TaxiChatGroup
import com.example.soap.Domain.Models.Taxi.TaxiRoom
import com.example.soap.Domain.Models.Taxi.TaxiUser
import kotlinx.coroutines.flow.StateFlow
import java.util.Date


interface TaxiChatViewModelProtocol {

    // MARK: - ViewModel Properties
    val state: StateFlow<TaxiChatViewModel.ViewState>
    val groupedChats: StateFlow<List<TaxiChatGroup>>
    val taxiUser: StateFlow<TaxiUser?>
    var fetchedDateSet: MutableSet<Date>
    val room: StateFlow<TaxiRoom>
    val isUploading: StateFlow<Boolean>

    // MARK: - Computed Properties
    val isLeaveRoomAvailable: Boolean
    val isCommitSettlementAvailable: Boolean
    val isCommitPaymentAvailable: Boolean

    // MARK: - Functions
    suspend fun setup()

    suspend fun fetchChats(before: Date)
    suspend fun fetchInitialChats()
    suspend fun sendChat(message: String, type: TaxiChat.ChatType)
    suspend fun leaveRoom()
    suspend fun commitSettlement()
    suspend fun commitPayment()
    suspend fun sendImage(image: Bitmap)
    fun switchRoom(newRoom: TaxiRoom)
}